use std::sync::Arc;

use axum::{response::IntoResponse, Json};
use chrono::{DateTime, Duration, Utc};
use hmac::{Hmac, Mac};
use hyper::StatusCode;
use jwt::{SignWithKey, VerifyWithKey};
use serde::{Deserialize, Serialize};
use sha2::Sha256;
use tokio_postgres::Client;
use uuid::Uuid;

#[derive(Debug, Deserialize, Serialize)]
pub struct RegisterUserPayload {
    pub username: String,
    pub email: String,
    pub password: String,
}

#[derive(Debug, Deserialize, Serialize)]
pub struct LoginUserPayload {
    pub email: String,
    pub password: String,
}

#[derive(Debug, Deserialize, Serialize)]
pub struct UserJWTInfo {
    pub email: String,
    pub expire_date: DateTime<Utc>,
}

impl UserJWTInfo {
    pub fn from_email(email: String) -> Self {
        let expire_date = Utc::now() + Duration::days(2);
        UserJWTInfo { email, expire_date }
    }
}

#[derive(Debug, Deserialize, Serialize)]
pub struct DBUser {
    pub user_id: String, // UUID
    pub username: String,
    pub email: String,
    pub password: String,
}

#[derive(Debug, Deserialize, Serialize)]
pub struct DBSession {
    session_id: String, // UUID
    user_id: String,    // UUID
    expire_date: DateTime<Utc>,
}

impl DBSession {
    pub fn new(user_id: String, expire_date: DateTime<Utc>) -> Self {
        DBSession {
            user_id,
            expire_date,
            session_id: Uuid::new_v4().to_string(),
        }
    }
}

pub const APP_SECRET: &'static [u8] = b"super-secret-key";

pub async fn register_user_route(
    payload: Json<serde_json::Value>,
    db_client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, StatusCode> {
    tracing::debug!("Enters register user route...");
    let RegisterUserPayload {
        username,
        email,
        password,
    } = match serde_json::from_value(payload.0.clone()) {
        Ok(r) => r,
        Err(err) => {
            tracing::error!(
                "Error parsing register user route payload into object: {}",
                payload.0
            );
            tracing::error!("Error: {}", err);
            Err(StatusCode::BAD_REQUEST)?
        }
    };

    tracing::debug!("Trying to register {}...", username);

    if let Some(conn) = db_client.as_ref() {
        tracing::debug!("Connection to DB obtained!");
        tracing::debug!("Checking if {} is not already in use...", email);

        let result = conn
            .query("SELECT * FROM \"User\" WHERE email=$1", &[&email])
            .await
            .unwrap();
        //.map_err(|_| StatusCode::BAD_REQUEST)?;

        if !result.is_empty() {
            tracing::error!("Email {} is already in use!", email);
            Err(StatusCode::BAD_REQUEST)?;
        }

        tracing::debug!("User does not exists, creating user...");
        tracing::debug!("Creating user id...");
        let user_id = Uuid::new_v4().to_string();

        tracing::debug!("Inserting into DB...");
        let statement = match conn
            .prepare(
                "INSERT INTO \"User\" (user_id, username, email, password) VALUES ($1, $2, $3, $4)",
            )
            .await
        {
            Ok(s) => s,
            Err(err) => {
                tracing::error!(
                    "An error has occurred when inserting the user {} on the DB",
                    username
                );
                tracing::error!("Error: {}", err);
                Err(StatusCode::INTERNAL_SERVER_ERROR)?
            }
        };

        let modified_rows = match conn
            .execute(&statement, &[&user_id, &username, &email, &password])
            .await
        {
            Ok(r) => r,
            Err(err) => {
                tracing::error!("Couldn't insert the user {} on the DB!", username);
                tracing::error!("Error: {}", err);
                Err(StatusCode::INTERNAL_SERVER_ERROR)?
            }
        };

        if modified_rows != 1 {
            tracing::error!("No user inserted in DB!");
            Err(StatusCode::INTERNAL_SERVER_ERROR)?;
        }
    }
    tracing::debug!("Registered {} with email {}...", username, email);

    Ok(())
}

pub async fn login_user_route(
    payload: Json<serde_json::Value>,
    db_client: Arc<Option<Client>>,
) -> Result<impl IntoResponse, StatusCode> {
    tracing::debug!("Login in a user...");
    // TODO Handle error from incorrect JSON value...
    let LoginUserPayload { email, password } = serde_json::from_value(payload.0).unwrap();
    tracing::debug!(
        "Payload extracted successfully from {} login attempt",
        email
    );

    if let Some(conn) = db_client.as_ref() {
        tracing::debug!("DB Connection found");

        tracing::debug!("Selecting user from the database...");
        let row = conn
            .query_one("SELECT user_id FROM \"User\" WHERE email=$1 AND password=$2", &[&email, &password])
            .await
            .unwrap();
        let user_id: String = row.try_get("user_id").unwrap();
        tracing::debug!("Username with id {} found!", user_id);

        tracing::debug!("Creating session...");
        let session = DBSession::new(user_id, Utc::now() + Duration::minutes(30));
        let record_added = conn
            .execute(
                "INSERT INTO \"Session\" (session_id, user_id, expire_date) VALUES ($1, $2, $3)",
                &[&session.session_id, &session.user_id, &session.expire_date],
            )
            .await
            .unwrap()
            == 1;

        if !record_added {
            tracing::error!("Couldn't insert the record into the DB!");
            Err(StatusCode::INTERNAL_SERVER_ERROR)?;
        }

        tracing::debug!("Generating JWT...");
        let token = generate_jwt(APP_SECRET, UserJWTInfo::from_email(email));

        tracing::debug!("JWT generated!");
        Ok(token)
    } else {
        Ok(String::new())
    }
}

pub fn generate_jwt(secret: &[u8], token_info: UserJWTInfo) -> String {
    let secret_key: Hmac<Sha256> = Hmac::new_from_slice(secret).unwrap();
    token_info.sign_with_key(&secret_key).unwrap()
}

pub fn extract_jwt(secret: &[u8], token: String) -> UserJWTInfo {
    let secret_key: Hmac<Sha256> = Hmac::new_from_slice(secret).unwrap();
    token.verify_with_key(&secret_key).unwrap()
}

use axum::{response::IntoResponse, Json};
use chrono::{DateTime, Duration, Utc};
use hmac::{Hmac, Mac};
use hyper::StatusCode;
use jwt::{SignWithKey, VerifyWithKey};
use serde::{Deserialize, Serialize};
use sha2::Sha256;

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

pub const APP_SECRET: &'static [u8] = b"super-secret-key";

pub async fn register_user_route(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, StatusCode> {
    // TODO Handle error from incorrect JSON value...
    tracing::debug!("Enters register user route...");
    let RegisterUserPayload {
        username,
        email,
        password,
    } = serde_json::from_value(payload.0).map_err(|_| StatusCode::BAD_REQUEST)?;

    tracing::debug!("Trying to register {}...", username);

    //TODO Connect to DB/Reuse connection and insert user info...
    tracing::debug!("Registered {}...", username);

    Ok(())
}

pub async fn login_user_route(
    payload: Json<serde_json::Value>,
) -> Result<impl IntoResponse, StatusCode> {
    // TODO Handle error from incorrect JSON value...
    let LoginUserPayload { email, password } =
        serde_json::from_value(payload.0).map_err(|_| StatusCode::BAD_REQUEST)?;

    // TODO Check on the DB if the user exists
    let user_exists_and_is_valid = true;

    if user_exists_and_is_valid {
        let token = generate_jwt(APP_SECRET, UserJWTInfo::from_email(email));
        Ok(token)
    } else {
        Err::<String, _>(StatusCode::BAD_REQUEST)
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

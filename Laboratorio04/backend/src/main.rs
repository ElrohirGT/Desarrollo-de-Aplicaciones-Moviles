use std::{net::SocketAddr, sync::Arc};

use axum::{
    routing::{get, post},
    Json, Router,
};
use tokio_postgres::{connect, Client, Error, NoTls};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const DB_CONNECTION_CONFIG: &'static str = "host=localhost port=5432 user=postgres dbname=lab04 connect_timeout=10";

#[tokio::main]
async fn main() -> Result<(), Error> {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "backend=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let addr = if cfg!(debug_assertions) {
        SocketAddr::from(([127, 0, 0, 1], 3000))
    } else {
        SocketAddr::from(([0, 0, 0, 0], 3000))
    };

    let (client, connection) = tokio_postgres::connect(DB_CONNECTION_CONFIG, NoTls).await?;

    let db_connection_handle = tokio::spawn(connection);

    start_server_on(addr, client).await;
    if let Err(e) = db_connection_handle.await {
        tracing::error!("Connection with the DB couldn't be established!");
        tracing::error!("{:?}", e);
    }

    Ok(())
}

/// Starts a server on the specified address
async fn start_server_on(addr: SocketAddr, client: Client) {
    tracing::debug!("listening on {}", addr);

    axum::Server::bind(&addr)
        .serve(app(Arc::new(Some(client))).into_make_service())
        .await
        .unwrap();
}

/// Having a function that produces our app makes it easy to call it from tests
/// without having to create an HTTP server.
#[allow(dead_code)]
fn app(db_client: Arc<Option<Client>>) -> Router {
    let db_client_copy = db_client.clone();
    Router::new()
        .route("/", get(|| async { "Hello, World!" }))
        .route(
            "/json",
            post(|payload: Json<serde_json::Value>| async move {
                Json(serde_json::json!({ "data": payload.0 }))
            }),
        )
        .route("/register", post(move|p| backend::register_user_route(p, db_client)))
        .route("/login", post(move|p| backend::login_user_route(p, db_client_copy)))
}

#[cfg(test)]
mod tests {
    use super::*;
    use axum::{
        body::Body,
        http::{self, Request, StatusCode},
    };
    use backend::{extract_jwt, LoginUserPayload, RegisterUserPayload, APP_SECRET};
    use hyper::Method;

    use serde_json::{json, Value};
    use std::net::{SocketAddr, TcpListener};
    use tower::Service; // for `call`
    use tower::ServiceExt; // for `oneshot` and `ready`

    #[tokio::test]
    async fn hello_world() {
        let app = app(Arc::default());

        // `Router` implements `tower::Service<Request<Body>>` so we can
        // call it like any tower service, no need to run an HTTP server.
        let response = app
            .oneshot(Request::builder().uri("/").body(Body::empty()).unwrap())
            .await
            .unwrap();

        assert_eq!(response.status(), StatusCode::OK);

        let body = hyper::body::to_bytes(response.into_body()).await.unwrap();
        assert_eq!(&body[..], b"Hello, World!");
    }

    #[tokio::test]
    async fn json() {
        let app = app(Arc::default());

        let response = app
            .oneshot(
                Request::builder()
                    .method(http::Method::POST)
                    .uri("/json")
                    .header(http::header::CONTENT_TYPE, mime::APPLICATION_JSON.as_ref())
                    .body(Body::from(
                        serde_json::to_vec(&json!([1, 2, 3, 4])).unwrap(),
                    ))
                    .unwrap(),
            )
            .await
            .unwrap();

        assert_eq!(response.status(), StatusCode::OK);

        let body = hyper::body::to_bytes(response.into_body()).await.unwrap();
        let body: Value = serde_json::from_slice(&body).unwrap();
        assert_eq!(body, json!({ "data": [1, 2, 3, 4] }));
    }

    #[tokio::test]
    async fn not_found() {
        let app = app(Arc::default());

        let response = app
            .oneshot(
                Request::builder()
                    .uri("/does-not-exist")
                    .body(Body::empty())
                    .unwrap(),
            )
            .await
            .unwrap();

        assert_eq!(response.status(), StatusCode::NOT_FOUND);
        let body = hyper::body::to_bytes(response.into_body()).await.unwrap();
        assert!(body.is_empty());
    }

    #[tokio::test]
    async fn register_user_route() {
        let app = app(Arc::default());

        let value = RegisterUserPayload {
            username: "ElrohirGT".to_owned(),
            email: "elrohirgt@gmail.com".to_owned(),
            password: "123456".to_owned(),
        };

        let body = serde_json::to_string(&value).unwrap();
        println!("The body of the request is: {:?}", body);

        let response = app
            .oneshot(
                Request::builder()
                    .method(Method::POST)
                    .uri("/register")
                    .header(http::header::CONTENT_TYPE, mime::APPLICATION_JSON.as_ref())
                    .body(Body::from(body))
                    .unwrap(),
            )
            .await
            .unwrap();

        println!("Response reached: {:?}", response);
        assert_eq!(response.status(), StatusCode::OK);
    }

    #[tokio::test]
    async fn login_user_route() {
        let app = app(Arc::default());

        let email = "elrohirgt@gmail.com".to_string();
        let value = LoginUserPayload {
            email: email.clone(),
            password: "12345".to_string(),
        };

        let body = serde_json::to_string(&value).unwrap();
        println!("The body of the request is: {:?}", body);

        let response = app
            .oneshot(
                Request::builder()
                    .method(Method::POST)
                    .uri("/login")
                    .header(http::header::CONTENT_TYPE, mime::APPLICATION_JSON.as_ref())
                    .body(Body::from(body))
                    .unwrap(),
            )
            .await
            .unwrap();

        println!("Response reached: {:?}", response);
        assert_eq!(response.status(), StatusCode::OK);

        let body = hyper::body::to_bytes(response.into_body()).await.unwrap();
        let body = String::from_utf8(body.to_vec()).unwrap();
        let received = extract_jwt(APP_SECRET, body);
        assert_eq!(received.email, email);
    }

    // You can also spawn a server and talk to it like any other HTTP server:
    #[tokio::test]
    async fn the_real_deal() {
        let listener = TcpListener::bind("0.0.0.0:0".parse::<SocketAddr>().unwrap()).unwrap();
        let addr = listener.local_addr().unwrap();

        tokio::spawn(async move {
            axum::Server::from_tcp(listener)
                .unwrap()
                .serve(app(Arc::default()).into_make_service())
                .await
                .unwrap();
        });

        let client = hyper::Client::new();

        let response = client
            .request(
                Request::builder()
                    .uri(format!("http://{}", addr))
                    .body(Body::empty())
                    .unwrap(),
            )
            .await
            .unwrap();

        let body = hyper::body::to_bytes(response.into_body()).await.unwrap();
        assert_eq!(&body[..], b"Hello, World!");
    }

    // You can use `ready()` and `call()` to avoid using `clone()`
    // in multiple request
    #[tokio::test]
    async fn multiple_request() {
        let mut app = app(Arc::default());

        let request = Request::builder().uri("/").body(Body::empty()).unwrap();
        let response = app.ready().await.unwrap().call(request).await.unwrap();
        assert_eq!(response.status(), StatusCode::OK);

        let request = Request::builder().uri("/").body(Body::empty()).unwrap();
        let response = app.ready().await.unwrap().call(request).await.unwrap();
        assert_eq!(response.status(), StatusCode::OK);
    }
}

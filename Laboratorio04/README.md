# Laboratorio 4
Aplicación de registro y login de usuarios usando un JWT. El cliente está hecho en android y el backend en rust.

Todo lo que el proyecto necesita para correr se encuentra dentro del archivo `default.nix`. Para utilizarlo puedes instalar [Nix](https://nixos.org/).

Una vez instalado simplemente corre (dentro de la carpeta root del repo):
```
nix-shell
```

Este comando instalará todas las dependencias y te creará una nueva sesión de tu terminal con todas las dependencias disponibles.

**Para correr el backend** simplemente corre (desde el root del repo):
```
nix-shell
cd backend
cargo run
```

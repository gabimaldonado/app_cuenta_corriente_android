package com.example.appcuentacorriente;

// Lo que se manda a login.php: {"email": "...", "password": "..."}
public class LoginPedido {
    String email;
    String password;

    public LoginPedido(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
package com.medilabo.microauth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity class representing user credentials in the database.
 * This class maps to the "user_credentials" table and holds the
 * information required for user authentication and storage.
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_credentials")
public class UserCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "name")
    private String name;
    @Column(name = "email")
    private String email;
    @Column(name = "password")
    private String password;
}

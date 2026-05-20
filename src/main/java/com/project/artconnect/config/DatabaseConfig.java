package com.project.artconnect.config;

/**
 * Database configuration constants.
 * 
 * Set USE_DATABASE to true to connect to Supabase PostgreSQL.
 * Set USE_DATABASE to false to use in-memory data (no database needed).
 */
public class DatabaseConfig {

    // ==========================================
    // SWITCH: true = Supabase DB, false = InMemory
    // ==========================================
    public static final boolean USE_DATABASE = true;

    // ==========================================
    // Supabase PostgreSQL connection settings
    // ==========================================
    public static final String URL = "jdbc:postgresql://aws-1-eu-west-2.pooler.supabase.com:6543/postgres?prepareThreshold=0";
    public static final String USER = "postgres.squtwhavrtsekeqsrngp";
    public static final String PASSWORD = "";
}

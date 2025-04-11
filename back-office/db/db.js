import { config as dotenvConfig } from 'dotenv';
import knex from "knex";

dotenvConfig();

const config = {
    client: 'pg',
    connection: {
        host: process.env.DB_HOST,
        port: process.env.DB_PORT || 5432,
        user: process.env.DB_USER,
        password: process.env.DB_PASSWORD,
        database: process.env.DB_NAME,
        ssl: {
            rejectUnauthorized: false
        }
    }
};

const db = knex(config);
export default db;
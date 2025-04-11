import db from "../db/db.js";

class FineRepository {
    archiveFine(id) {
        return db('fine_reports').where({ id }).update({ archive: true }).returning('*');
    }
}
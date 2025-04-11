import db from "../db/db.js";

class ReportRepository {
    archiveReport(id) {
        return db('fine_reports').where({ id }).update({ archive: true }).returning('*');
    }
}

const reportRepository = new ReportRepository();
export default reportRepository;
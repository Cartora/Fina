import { createError } from "../errors/errors.js";
import reportRepository from "../repositories/reportRepository.js";

class ReportService {
    async archiveReport(id) {
        const [archivedReport] = await reportRepository.archiveReport(id);
        if(!archivedReport) {
            throw createError(404, "The inserted id was not found")
        }
        return archivedReport;
    }
}

const reportService = new ReportService();
export default reportService;
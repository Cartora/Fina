import reportRepository from "../repositories/reportRepository.js";

class ReportService {
    async archiveReport(id) {
        const [archivedReport] = await reportRepository.archiveReport(id);
        return archivedReport;
    }
}

const reportService = new ReportService();
export default reportService;
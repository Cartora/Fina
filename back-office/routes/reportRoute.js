import reportService from "../services/reportService.js";
import express from 'express';

const reportRoute = express.Router();

reportRoute.patch("/archive/:id", async (req, res, next) => {
    try {
        const archivedReport = await reportService.archiveReport(req.params.id);
        res.send(archivedReport);
    } catch(error) {
        next(error);
    }
})

export default reportRoute;
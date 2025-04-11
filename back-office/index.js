import { config } from 'dotenv';
import express from 'express';
import reportRoute from './routes/reportRoute.js';
import { errorHandler } from './errors/errors.js';

config();

const app = express();
const PORT =  process.env.PORT || 3000;

app.use(express.json());

app.use("/reports", reportRoute)
app.listen(PORT, () => {
  console.log(`Back office server is running on port ${PORT}`);
});
app.use(errorHandler)
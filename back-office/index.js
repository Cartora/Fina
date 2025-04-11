import { config as dotenvConfig } from 'dotenv';
import express from 'express';

dotenvConfig();

const app = express();
const PORT =  3000;

app.use(express.json());
app.listen(PORT, () => {
  console.log(`Back office server is running on port ${PORT}`);
});
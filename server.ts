import express from "express";
import path from "path";
import fs from "fs";
import { createServer as createViteServer } from "vite";

async function startServer() {
  const app = express();
  const PORT = 3000;

  // Middleware to parse JSON payloads
  app.use(express.json());

  // API endpoint for crash logs
  app.post("/api/log-crash", (req, res) => {
    try {
      const { errorMessage, codeSource } = req.body;
      
      const debugLogsDir = path.join(process.cwd(), 'logs', 'Debug logs');
      
      // Ensure directory exists
      if (!fs.existsSync(debugLogsDir)) {
        fs.mkdirSync(debugLogsDir, { recursive: true });
      }

      // Date & Time formatting: dd-mm-yyyy _ hh_mm_ss
      const now = new Date();
      const dd = String(now.getDate()).padStart(2, '0');
      const mm = String(now.getMonth() + 1).padStart(2, '0');
      const yyyy = now.getFullYear();
      
      const hh = String(now.getHours()).padStart(2, '0');
      const min = String(now.getMinutes()).padStart(2, '0');
      const ss = String(now.getSeconds()).padStart(2, '0');

      // The file name format: Crash log (dd-mm-yyyy) - (hh_mm_ss).txt
      const filename = `Crash log (${dd}-${mm}-${yyyy}) - (${hh}_${min}_${ss}).txt`;
      const filePath = path.join(debugLogsDir, filename);

      const logContent = `- Wat ging er fout: ${errorMessage || 'Unknown Error'}\n- Wat of welke deel van de code heeft dit veroorzaakt: ${codeSource || 'Browser process / Unknown code line'}\n`;

      fs.writeFileSync(filePath, logContent, 'utf8');
      console.log(`Successfully wrote crash log: ${filePath}`);
      res.json({ success: true, file: filename });
    } catch (error) {
      console.error("Failed to write crash log server-side:", error);
      res.status(500).json({ success: false, error: String(error) });
    }
  });

  // Vite middleware for development
  if (process.env.NODE_ENV !== "production") {
    const vite = await createViteServer({
      server: { middlewareMode: true },
      appType: "spa",
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(process.cwd(), 'dist');
    app.use(express.static(distPath));
    app.get('*', (req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  // Gracefully handle any express internal uncaught server crashes as well
  app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
    try {
      const debugLogsDir = path.join(process.cwd(), 'logs', 'Debug logs');
      if (!fs.existsSync(debugLogsDir)) {
        fs.mkdirSync(debugLogsDir, { recursive: true });
      }

      const now = new Date();
      const dd = String(now.getDate()).padStart(2, '0');
      const mm = String(now.getMonth() + 1).padStart(2, '0');
      const yyyy = now.getFullYear();
      const hh = String(now.getHours()).padStart(2, '0');
      const min = String(now.getMinutes()).padStart(2, '0');
      const ss = String(now.getSeconds()).padStart(2, '0');

      const filename = `Crash log (${dd}-${mm}-${yyyy}) - (${hh}_${min}_${ss}).txt`;
      const filePath = path.join(debugLogsDir, filename);

      const logContent = `- Wat ging er fout: ${err?.message || 'Server Exception'}\n- Wat of welke deel van de code heeft dit veroorzaakt: ${err?.stack || 'server.ts'}\n`;

      fs.writeFileSync(filePath, logContent, 'utf8');
    } catch (logErr) {
      console.error("Failed to self-log server error", logErr);
    }
    res.status(500).send("Internal Server Error");
  });

  app.listen(PORT, "0.0.0.0", () => {
    console.log(`Server running on port ${PORT}`);
  });
}

startServer();

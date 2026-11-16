#!/usr/bin/env node
import { runCli } from "../src/cli.js";

runCli()
  .then((code) => {
    if (typeof code === "number") {
      process.exitCode = code;
    }
  })
  .catch((err) => {
    process.stderr.write(`${err.message || String(err)}\n`);
    process.exitCode = 1;
  });

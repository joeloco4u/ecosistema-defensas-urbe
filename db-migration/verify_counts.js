const { Client } = require('pg');
const client = new Client({
  host: 'aws-1-us-east-2.pooler.supabase.com',
  port: 5432,
  user: 'postgres.vuuezzibhsltyfaoscgu',
  password: '3cFEPz2pSrgMQM9',
  database: 'postgres',
  ssl: { rejectUnauthorized: false },
});
async function main() {
  await client.connect();
  const tables = ['temp_espacio', 'temp_seccion', 'temp_bloque_hora', 'temp_persona', 'temp_horario'];
  for (const t of tables) {
    const r = await client.query(`SELECT COUNT(*) AS c FROM "${t}"`);
    console.log(`${t}: ${r.rows[0].c} filas`);
  }
  await client.end();
}
main().catch((e) => { console.error('FALLO:', e.message); process.exit(1); });

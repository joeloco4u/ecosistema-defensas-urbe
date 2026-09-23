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
    const res = await client.query(
      `SELECT column_name, data_type, is_nullable FROM information_schema.columns
       WHERE table_name = $1 ORDER BY ordinal_position`,
      [t],
    );
    console.log(`\n=== ${t} ===`);
    if (res.rows.length === 0) {
      console.log('  (no existe)');
    } else {
      res.rows.forEach((r) => console.log(`  ${r.column_name} ${r.data_type} ${r.is_nullable === 'NO' ? 'NOT NULL' : ''}`));
      const count = await client.query(`SELECT COUNT(*) FROM "${t}"`);
      console.log(`  -> filas actuales: ${count.rows[0].count}`);
    }
  }
  await client.end();
}

main().catch((e) => { console.error('FALLO:', e.message); process.exit(1); });

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
  const res = await client.query(
    `SELECT column_name, data_type, is_nullable, udt_name FROM information_schema.columns
     WHERE table_name = 'horarios_clases' ORDER BY ordinal_position`
  );
  if (res.rows.length === 0) {
    console.log('NO EXISTE la tabla horarios_clases');
  } else {
    console.log('=== horarios_clases ===');
    res.rows.forEach((r) => console.log(`  ${r.column_name} ${r.udt_name} ${r.data_type} ${r.is_nullable === 'NO' ? 'NOT NULL' : ''}`));
    const c = await client.query('SELECT COUNT(*) AS n FROM horarios_clases');
    console.log('filas:', c.rows[0].n);
    const s = await client.query('SELECT * FROM horarios_clases LIMIT 3');
    console.log('muestra:', JSON.stringify(s.rows, null, 2));
  }
  await client.end();
}
main().catch((e) => { console.error('FALLO:', e.message); process.exit(1); });

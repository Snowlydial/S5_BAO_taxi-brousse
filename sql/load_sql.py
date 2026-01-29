from __future__ import annotations
import argparse
import os
import subprocess
import sys
from pathlib import Path


def run_psql(sql_path: Path, user: str, host: str, port: int, password: str) -> None:
    env = os.environ.copy()
    env['PGPASSWORD'] = password

    # Build psql command without -d to match calling `psql -U <user> -f <file>`.
    cmd = ['psql', '-U', user, '-h', host, '-p', str(port), '-f', str(sql_path)]

    print(f"\n=== Executing: {sql_path.name} ===")
    print("Command:", ' '.join(cmd))

    try:
        subprocess.run(cmd, check=True, env=env)
        print(f"OK: {sql_path.name}")
    except FileNotFoundError:
        print("Error: `psql` executable not found in PATH.", file=sys.stderr)
        sys.exit(2)
    except subprocess.CalledProcessError as e:
        print(f"Error: psql failed with return code {e.returncode}", file=sys.stderr)
        sys.exit(e.returncode)


def main() -> None:
    parser = argparse.ArgumentParser(description="Run project SQL files against Postgres (sequentially)")
    parser.add_argument('--data-file', default='dataV5-simple.sql', help='Data SQL filename located in `sql` folder (default: dataV5-simple.sql)')
    parser.add_argument('--db-user', default='postgres', help='Postgres user (default: postgres)')
    parser.add_argument('--db-name', default='taxibroussedb', help='Postgres database name (default: taxibroussedb)')
    parser.add_argument('--host', default='localhost', help='Postgres host (default: localhost)')
    parser.add_argument('--port', default=5432, type=int, help='Postgres port (default: 5432)')
    parser.add_argument('--password', default='snow', help='Postgres password (default: snow)')
    parser.add_argument('--skip-views', action='store_true', help='Skip loading views.sql')

    args = parser.parse_args()

    script_dir = Path(__file__).resolve().parent
    repo_root = script_dir.parent
    sql_dir = repo_root / 'sql'

    taxi_sql = sql_dir / 'Taxi_brousse.sql'
    views_sql = sql_dir / 'views.sql'
    data_sql = sql_dir / args.data_file

    for p in (taxi_sql, views_sql, data_sql):
        if p is None:
            continue
        if not p.exists():
            if p == views_sql and args.skip_views:
                print(f"Skipping missing views file: {p}")
                continue
            print(f"Error: required SQL file not found: {p}", file=sys.stderr)
            sys.exit(1)

    # Run Taxi_brousse.sql without -d so the script's internal DROP/CREATE/
    # \connect commands can execute correctly.
    run_psql(taxi_sql, args.db_user, args.host, args.port, args.password)

    # Run views.sql (optional to skip)
    if not args.skip_views:
        # views.sql should be applied to the created DB (use db_name)
        run_psql(views_sql, args.db_user, args.host, args.port, args.password)
    else:
        print("Skipping views.sql as requested")

    # Run data file
    run_psql(data_sql, args.db_user, args.host, args.port, args.password)

    print("\nAll scripts executed.")


if __name__ == '__main__':
    main()

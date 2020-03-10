CREATE OR REPLACE FUNCTION refreshallmaterializedviews(schema_arg text)
RETURNS integer AS
$BODY$
DECLARE
r RECORD;

BEGIN
	RAISE NOTICE 'Refreshing materialized view in schema %', schema_arg;
	if pg_is_in_recovery() then 
		return 1;
	else
		FOR r IN SELECT matviewname FROM pg_matviews WHERE schemaname = schema_arg 
		LOOP
			RAISE NOTICE 'Refreshing %.%', schema_arg, r.matviewname;
			EXECUTE 'REFRESH MATERIALIZED VIEW ' || schema_arg || '.' || r.matviewname || ' WITH DATA'; 
		END LOOP;
	end if;
	RETURN 1;
END 
$BODY$
LANGUAGE plpgsql VOLATILE
COST 100;
ALTER FUNCTION refreshallmaterializedviews(text)
OWNER TO postgres;
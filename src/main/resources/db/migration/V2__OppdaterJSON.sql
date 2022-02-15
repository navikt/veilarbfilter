UPDATE EnhetFilter
SET valgte_filter = replace(valgte_filter::TEXT,'"kjonn": []','"kjonn": null')::jsonb;
USE hospital_resource_db;
UPDATE doctors SET date_of_birth = '1985-06-15' WHERE date_of_birth IS NULL OR CAST(date_of_birth AS CHAR) = '0000-00-00';
UPDATE patients SET date_of_birth = '1990-01-01' WHERE date_of_birth IS NULL OR CAST(date_of_birth AS CHAR) = '0000-00-00';

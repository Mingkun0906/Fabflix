ALTER TABLE sales MODIFY COLUMN id INTEGER NOT NULL;

ALTER TABLE sales DROP PRIMARY KEY;

ALTER TABLE sales ADD PRIMARY KEY (id, movieId);

ALTER TABLE sales MODIFY COLUMN id INTEGER NOT NULL AUTO_INCREMENT;

ALTER TABLE sales ADD COLUMN quantity INTEGER NOT NULL DEFAULT 1;

UPDATE sales SET quantity = 1 WHERE quantity IS NULL;
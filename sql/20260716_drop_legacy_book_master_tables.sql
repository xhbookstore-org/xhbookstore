-- Optional cleanup. Run only after 20260716_borrow_detail_points_rule.sql and the V2 app are deployed,
-- and after confirming no external reporting job reads these legacy tables.
DROP TABLE IF EXISTS book_image;
DROP TABLE IF EXISTS book_info_history;
DROP TABLE IF EXISTS book_info;

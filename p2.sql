--
-- db2 -td"@" -f p2.sql
--
CONNECT TO CS157A@
--
--
DROP PROCEDURE P2.CUST_CRT@
DROP PROCEDURE P2.CUST_LOGIN@
DROP PROCEDURE P2.ACCT_OPN@
DROP PROCEDURE P2.ACCT_CLS@
DROP PROCEDURE P2.ACCT_DEP@
DROP PROCEDURE P2.ACCT_WTH@
DROP PROCEDURE P2.ACCT_TRX@
DROP PROCEDURE P2.ADD_INTEREST@
--
--
CREATE PROCEDURE P2.CUST_CRT
(IN p_name CHAR(15), IN p_gender CHAR(1), IN p_age INTEGER, IN p_pin INTEGER, OUT id INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF p_gender != 'M' AND p_gender != 'F' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid gender';
    ELSEIF p_age <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid age';
    ELSEIF p_pin < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid pin';
    ELSE
      INSERT INTO p2.customer(Name, Gender, Age, Pin) VALUES(p_name, p_gender, p_age, p_pin);
      SET err_msg = '-';
      SET id = (SELECT ID FROM p2.customer WHERE Name = p_name AND Pin = p_pin);
      SET sql_code = 0;
    END IF;
END@

CREATE PROCEDURE P2.CUST_LOGIN
(IN p_id INTEGER , IN p_pin INTEGER, OUT valid INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF p_pin < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid pin';
      SET valid = 0;
    ELSEIF p_id < 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid id';
      SET valid = 0;
    ELSEIF (SELECT COUNT(*) FROM p2.customer WHERE p2.customer.id = p_id AND p2.customer.pin = p_pin) = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Incorrect id or pin';
      SET valid = 0;
    ELSE
      SET sql_code = 0;
      SET err_msg = '-';
      SET valid = 1;
    END IF;
END@
--
CREATE PROCEDURE P2.ACCT_OPN
(IN p_id INTEGER, IN p_bal INTEGER, IN p_type CHAR(1), OUT number INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF p_type != 'C' AND p_type != 'S' THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid Account Type';
    ELSEIF p_bal <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid balance';
    ELSEIF (SELECT Count(*) FROM p2.customer WHERE p2.customer.ID = p_id) = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid customer id';
    ELSE
      INSERT INTO p2.account(ID, Balance, Type, Status) VALUES(p_id, p_bal, p_type, 'A');
      SET err_msg = '-';
      SET number = (SELECT Number FROM p2.account WHERE ID = p_id AND Balance = p_bal AND Type = p_type);
      SET sql_code = 0;
    END IF;
END@
--
CREATE PROCEDURE P2.ACCT_CLS
(IN p_num INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF (SELECT COUNT(*) FROM p2.account WHERE Number = p_num) = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSE
      UPDATE p2.account SET p2.account.Balance = 0, p2.account.Status = 'I' WHERE Number = p_num;
      SET err_msg = '-';
      SET sql_code = 0;
    END IF;
END@
--
CREATE PROCEDURE P2.ACCT_DEP
(IN p_num INTEGER, IN p_amt INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF (SELECT COUNT(*) FROM p2.account WHERE Number = p_num AND p2.account.Status = 'A') = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSEIF p_amt <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid amount';
    ELSE
      UPDATE p2.account SET p2.account.Balance = Balance + p_amt WHERE Number = p_num;
      SET err_msg = '-';
      SET sql_code = 0;
    END IF;
END@
--
CREATE PROCEDURE P2.ACCT_WTH
(IN p_num INTEGER, IN p_amt INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    IF (SELECT COUNT(*) FROM p2.account WHERE Number = p_num AND p2.account.Status = 'A') = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid account number';
    ELSEIF p_amt <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid amount';
    ELSEIF (SELECT Balance FROM p2.account WHERE p2.account.Number = p_num) < p_amt THEN
      SET sql_code = -100;
      SET err_msg = 'Not enough funds';
    ELSE
      UPDATE p2.account SET p2.account.Balance = Balance - p_amt WHERE Number = p_num;
      SET err_msg = '-';
      SET sql_code = 0;
    END IF;
END@
--
CREATE PROCEDURE P2.ACCT_TRX
(IN src Integer, IN dest INTEGER, IN p_amt INTEGER, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
    DECLARE v_int INTEGER;
    DECLARE v_out CHAR(100);
    IF (SELECT COUNT(*) FROM p2.account WHERE p2.account.Number = src  AND p2.account.Status = 'A') = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid source account number';
    ELSEIF (SELECT COUNT(*) FROM p2.account WHERE p2.account.Number = dest AND p2.account.Status = 'A') = 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid destination account number';
    ELSEIF p_amt <= 0 THEN
      SET sql_code = -100;
      SET err_msg = 'Invalid amount';
    ELSEIF (SELECT p2.account.Balance FROM p2.account WHERE p2.account.Number = src) < p_amt THEN
      SET sql_code = -100;
      SET err_msg = 'Not enough funds';
    ELSE
      SET v_int = -1;
      SET v_out = '';
      CALL p2.ACCT_DEP(dest, p_amt, v_int, v_out);
      CALL p2.ACCT_WTH(src, p_amt, v_int, v_out);
      SET err_msg = v_out;
      SET sql_code = v_int;
    END IF;
END@
--
CREATE PROCEDURE P2.ADD_INTEREST
(IN svg DOUBLE, IN chk DOUBLE, OUT sql_code INTEGER, OUT err_msg CHAR(100))
LANGUAGE SQL
  BEGIN
      DECLARE savings DOUBLE;
      DECLARE checking DOUBLE;
      SET savings = 1 + svg;
      SET checking = 1 + chk;
      UPDATE p2.account SET Balance = (Balance * savings) WHERE Type = 'S' AND Status = 'A';
      UPDATE p2.account SET Balance = (Balance * checking) WHERE Type = 'C' AND Status = 'A';
      SET err_msg = '-';
      SET sql_code = 0;
END@
--
TERMINATE@
--
--

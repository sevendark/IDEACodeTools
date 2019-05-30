package com.sevendark.ai.plugin.lib.sql.parser;

public class SqlParserVisitorTest {

    private static String sql101 = "select * from table01";

    private static String sql102 = "select id, id2 from table01";

    private static String sql103 = "select " +
            "TBL_01.AAA, " +
            "TBL_01.BBB, " +
            "TBL_01.CCC, " +
            "TBL_03.AAA, " +
            "TBL04.AAA, " +
            "TBL04.BBB, " +
            "TBL04.CCC, " +
            "TBL_02.ID   " +
            "from " +
            "TBL_01   " +
            "left outer join " +
            "TBL_02   " +
            "on TBL_01.ID = TBL_02.ID    " +
            "and TBL_01.O_ID = TBL_02.O_ID    " +
            "left outer join " +
            "TBL_03   " +
            "on TBL_01.ID = TBL_03.E_ID    " +
            "and TBL_01.O_ID = TBL_03.O_ID    " +
            "left outer join " +
            "TBL04   " +
            "on TBL_01.ID = TBL04.T_ID    " +
            "and TBL_01.O_ID = TBL04.O_ID    " +
            "where " +
            "TBL_01.ID = 1234    " +
            "and TBL_02.STATUS = 'Good'    " +
            "and TBL_01.COL_01 = 'Air'    " +
            "and TBL04.O_ID = 546 ";

    private static String sql104 = "select tbl_01.id P_ID, " +
            "tbl_01.ID O_ID, " +
            "tbl_02.NAME O_NAME, " +
            "tbl_03.num NUM, " +
            "tbl_01.r_id R_ID, " +
            "tbl_01.CON CON, " +
            "tbl_01.`STATUS`, " +
            "tbl_04.ffn FFN, " +
            "tbl_04.ggw GGW, " +
            "tbl_01.PCY PCY, " +
            "tbl_01.way WAY, " +
            "ifnull(`tbl_09`.`ttl`,`tbl_06`.`ttl`) AS `tt_tl`, " +
            "(case when (`tbl_08`.`ddv` > 0) then `tbl_07`.`ccoo` else NULL end) AS `ccoo`, " +
            "(case when (`tbl_08`.`ddv` > 0) then `tbl_07`.`ttll` else NULL end) AS `ttll`, " +
            "tbl_05.msg MSG " +
            "from tbl_01 " +
            "join tbl_03  " +
            "on tbl_01.O_ID = tbl_03.O_ID  " +
            "and tbl_01.ID = tbl_03.i_id " +
            "and tbl_03.status <> 'BAD'  " +
            "and tbl_01.TTYP = 'Some' " +
            "join tbl_05  " +
            "on tbl_01.id = tbl_05.t_id " +
            "join tbl_06 " +
            "on tbl_06.p_id = tbl_03.id " +
            "join tbl_02  " +
            "on tbl_01.O_ID = tbl_02.ID " +
            "join tbl_07  " +
            "on tbl_04.ID = tbl_07.O_ID  " +
            "and tbl_04.lcc = tbl_07.lcc " +
            "join tbl_08  " +
            "on tbl_01.r_id = tbl_08.r_id  " +
            "and tbl_08.success = true " +
            "left join tbl_09  " +
            "on tbl_01.id = tbl_09.v_id  " +
            "and tbl_04.otype = 'Red' " +
            "left join tbl_04  " +
            "on tbl_02.O_ID = tbl_04.O_ID  " +
            "and tbl_01.email = tbl_04.email " +
            "where tbl_01.O_ID = 789 " +
            "and tbl_06.l_id is not null " +
            "group By tbl_02.r_id " +
            "order By tbl_01.coo desc, tbl_06.ID asc, tbl_03.num desc, tbl_05.way asc " +
            "limit 10, 10";

    private static String sql105 = "select t.id from (select id, iis from table2) as t where t.iis = 343";

    private static String sql106 = "select t.id from (select id, iis from table2) as t where t.iis in (343, 45857, 1235)";

    private static String sql107 = "select t.id from t where t.iis is true";

    private static String sql201 = "delete from tbl_ett where e_id = 456 limit 10";

    private static String sql301 = "update tblkkkk  " +
            "set tblkkkk.cc = tblkkkk.cc + 5, " +
            "tblkkkk.oc = 12 " +
            "where tblkkkk.oid = 75  " +
            "and tblkkkk.eid = 45  " +
            "and tblkkkk.gid = 4 " +
            "and tblkkkk.sed = 'abcd'";

    private static String sql302 = "update tblkkkk  " +
            "set cc = cc + 5, " +
            "oc = 12 " +
            "where oid = 75  " +
            "and eid = 45  " +
            "and gid = 4 " +
            "and sed = 'abcd'";

    private static String sql401 = "insert into tbll values (1, 'a')";

    private static String sql402 = "insert into tbll values (1, 'a'), (2, 'b')";

    private static String sql403 = "insert into tbll(id, vv) values (1, 'a')";

    private static String sql404 = "insert into tbll(id, vv) values (1, 'a'), (2, 'b')";

    private static String sql405 = "insert into tbll(id, vv) select id, nn from tblse";

    public static void main(String[] args) {
        System.out.println(SqlParserVisitor.parse(sql107));
    }

}

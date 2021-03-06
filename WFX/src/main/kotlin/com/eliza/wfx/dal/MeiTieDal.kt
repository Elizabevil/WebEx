package com.eliza.wfx.dal

import com.eliza.wfx.model.MeiPaiModel
import com.eliza.wfx.utils.JDBCUtil.DbTools
import com.eliza.wfx.utils.JDBCUtil.DbTools.Companion.addUpdDel
import java.sql.SQLException
import kotlin.reflect.full.memberProperties


class MeiTieDal {
    fun nameToMethod(mei: MeiPaiModel, vararg frontAndLast: String): HashMap<String, String> {
        val hashMapOf = hashMapOf<String, String>()

        for (v in mei::class.memberProperties) {
            if (frontAndLast.size <= 1) {
                hashMapOf[frontAndLast[0] + v.toString()] = mei.contentPic

            } else {
                println(v.name) //shortTitle
                hashMapOf[frontAndLast[0] + v.toString() + frontAndLast[1]]


            }
        }
        return hashMapOf
    }


    /**
     * Update mei tie
     *
     * @param meiTie 新的信息
     * @param args 参数群，要修改的选项（属性名）
     * @param id 要修改的id
     * @return
     */
    fun updateMeiTie(meiTie: MeiPaiModel, vararg args: String, id: Int): Boolean {
        var flag = false
        var rs = DbTools.selectSql("SELECT 1 FROM w_meitie where m_id=${id} limit 1") //没有该信息提示添加
        rs ?: return flag
        if (!rs.first()) {
            println("不存在该 meitie,请添加")
            return flag
        }
        //修改信息
        rs.let {
            var sql = "update w_meitie set "
            if (args.isNotEmpty()) {
                for (i in args.indices) {
                    sql += "${args[i]}='${args[i]}'"
                }
                sql += "where m_id = '${id}'"
            }

            print(sql)
            try {
                rs.let {
                    if (rs.first()) {
                        val i: Int = DbTools.addUpdDel(sql)
                        if (i > 0) {
                            flag = true
                        }
                    } else {
                        println("--不存在数据请添加---")
                    }
                }
            } catch (e: SQLException) {
                e.printStackTrace()
            }
        }

        return flag
    }

    /**
     * Get meiTie all
     *
     * @param tableName 表名,默认从 w_meitie读取
     * @param args  不定参数,两两匹配[属性,值,属性,值] 最多三对
     * @return
     */
    fun getMeiTieAll(tableName: String? = null, vararg args: String): MutableList<MeiPaiModel>? {
        var sql = "select * from w_meitie"
        tableName?.let {
            if (args.isNotEmpty() && args.size % 2 == 0) {
                sql = "select * from $tableName where "
                for (i in 0..args.size step (2)) {
                    sql += "${args[i]}='${args[i + 1]}'"
                    if (args.size > 2) {
                        sql += " and ${args[i]}='${args[i + 1]}'  "

                    }
                }

            } else {
                println("---输入的参数有误,需重新输入--")
                println("至少输入一对 参数")
                return null
            }
        }
        println(sql)
        val list: MutableList<MeiPaiModel> = ArrayList()
        try {
            val rs = DbTools.selectSql(sql)
            while (rs!!.next()) {
                val userAll = UserDal().getUserAll("w_user", "u_id", rs.getLong("m_ownerId").toString())
                userAll?.let {
                    list.add(
                        MeiPaiModel(
                            rs.getLong("m_id"),
                            rs.getLong("m_ownerId"),
                            rs.getInt("m_nCollect"),
                            rs.getInt("m_nFavorite"),
                            rs.getString("m_shortTitle"),
                            rs.getString("m_content"),
                            rs.getString("m_coverPic"),
                            rs.getString("m_contentPic"),
                            userAll[0]
                        )

                    )
                }

            }
            if (list.isEmpty()) {
                println("--信息不存在--")
                return null
            }
            return list
        } catch (e: SQLException) {
            e.printStackTrace()
        }
        return null

    }


    /**
     * @param m_id 通过 meitie的id
     * @return 是否删除成功
     */
    fun deleteMeiTie(m_id: Int): Boolean {
        var flag = false
        val sql = "delete  from w_meitie where m_id=$m_id"
        val i = addUpdDel(sql)
        if (i > 0) {
            flag = true
        }
        return flag
    }
}
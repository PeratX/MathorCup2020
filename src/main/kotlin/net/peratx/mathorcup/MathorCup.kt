/**
 * 版权所有（C）2020 PeratX@iTXTech.org
 * MathorCup 2020参赛使用
 */

package net.peratx.mathorcup

val s = Shelves(25, 4)

fun main(){
    testCalc("S00913", "S00909", 4700)
    testCalc("S00913", "S01710", 12500)
    testCalc("S00102", "S01009", 15700)
    testCalc("S12315", "S06311", 51100)
    testCalc("S02510", "FH03", 12300)
    testCalc("S02614", "FH04", 16900)
    //testCalc("S03414", "FH04", 15800)
    testCalc("S00204", "FH09", 10400)
    testCalc("S00106", "FH11", 7600)
    testCalc("S02103", "FH13", 8200 + 11000)
}

fun testCalc(a: String, b: String, ans: Int){
    println("$a 到 $b 距离应为 $ans 计算为 " + s.calcDistance(a, b))
}

fun getCoord(a: String){
    println("$a 绝对坐标为 " + s.shelves[a.getShelf().getRealShelf()]!!.getCoordinate(a))
}

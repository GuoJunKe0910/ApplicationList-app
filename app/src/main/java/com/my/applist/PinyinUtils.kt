package com.my.applist

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

/**
 * 拼音工具类
 */
object PinyinUtils {
    
    private val format = HanyuPinyinOutputFormat().apply {
        caseType = HanyuPinyinCaseType.UPPERCASE
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }
    
    /**
     * 获取字符串的拼音全拼
     */
    fun getPinyin(str: String): String {
        val sb = StringBuilder()
        for (char in str) {
            if (char.isChineseChar()) {
                try {
                    val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(char, format)
                    if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                        sb.append(pinyinArray[0])
                    } else {
                        sb.append(char)
                    }
                } catch (e: BadHanyuPinyinOutputFormatCombination) {
                    sb.append(char)
                }
            } else {
                sb.append(char)
            }
        }
        return sb.toString()
    }
    
    /**
     * 获取字符串的首字母
     */
    fun getFirstLetter(str: String): String {
        if (str.isEmpty()) return "#"
        
        val firstChar = str[0]
        return if (firstChar.isChineseChar()) {
            try {
                val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(firstChar, format)
                if (pinyinArray != null && pinyinArray.isNotEmpty()) {
                    pinyinArray[0].substring(0, 1).uppercase()
                } else {
                    "#"
                }
            } catch (e: Exception) {
                "#"
            }
        } else if (firstChar.isLetter()) {
            firstChar.uppercase()
        } else {
            "#"
        }
    }
    
    /**
     * 判断字符是否为中文
     */
    private fun Char.isChineseChar(): Boolean {
        val ub = Character.UnicodeBlock.of(this)
        return ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                ub === Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS ||
                ub === Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                ub === Character.UnicodeBlock.GENERAL_PUNCTUATION ||
                ub === Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION ||
                ub === Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
    }
}



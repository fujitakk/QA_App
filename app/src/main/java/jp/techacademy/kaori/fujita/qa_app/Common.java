package jp.techacademy.kaori.fujita.qa_app;

import android.app.Application;

import java.util.HashSet;

/**
 * Created by fujita on 2016/10/17.
 */

public class Common extends Application {
	// グローバルに扱う変数
	HashSet<String> stringHashSet;
//	int stringHashSet;        // お気に入り

	/**
	 * 変数を初期化する
	 */
	public void init(){
		stringHashSet = new HashSet<String>();
	}
}

package jp.techacademy.kaori.fujita.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//import static jp.techacademy.kaori.fujita.qa_app.Const.FavKEY;

public class QuestionDetailActivity extends AppCompatActivity {

	private ListView mListView;
	private Question mQuestion;
	private QuestionDetailListAdapter mAdapter;

	private DatabaseReference mAnswerRef;
	private ImageView tapView; //課題
	private boolean favOn;
	private Common common;      // グローバル変数を扱うクラス

	private ChildEventListener mEventListener = new ChildEventListener() {
		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			HashMap map = (HashMap) dataSnapshot.getValue();

			String answerUid = dataSnapshot.getKey();

			for(Answer answer : mQuestion.getAnswers()) {
				// 同じAnswerUidのものが存在しているときは何もしない
				if (answerUid.equals(answer.getAnswerUid())) {
					return;
				}
			}

			String body = (String) map.get("body");
			String name = (String) map.get("name");
			String uid = (String) map.get("uid");

			Answer answer = new Answer(body, name, uid, answerUid);
			mQuestion.getAnswers().add(answer);
			mAdapter.notifyDataSetChanged();
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {

		}

		@Override
		public void onChildRemoved(DataSnapshot dataSnapshot) {

		}

		@Override
		public void onChildMoved(DataSnapshot dataSnapshot, String s) {

		}

		@Override
		public void onCancelled(DatabaseError databaseError) {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_question_detail);

		// 渡ってきたQuestionのオブジェクトを保持する
		Bundle extras = getIntent().getExtras();
		mQuestion = (Question) extras.get("question");

		setTitle(mQuestion.getTitle());

		// ListViewの準備
		mListView = (ListView) findViewById(R.id.listView);
		mAdapter = new QuestionDetailListAdapter(this, mQuestion);
		mListView.setAdapter(mAdapter);
		mAdapter.notifyDataSetChanged();

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				// ログイン済みのユーザーを収録する
				FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

				if (user == null) {
					// ログインしていなければログイン画面に遷移させる
					Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
					startActivity(intent);
				} else {
					// Questionを渡して回答作成画面を起動する
					Intent intent = new Intent(getApplicationContext(), AnswerSendActivity.class);
					intent.putExtra("question", mQuestion);
					startActivity(intent);
				}
			}
		});

		DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
		mAnswerRef = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(mQuestion.getGenre())).child(mQuestion.getQuestionUid()).child(Const.AnswersPATH);
		mAnswerRef.addChildEventListener(mEventListener);

		//課題　お気に入り画像のタップ
		tapView = (ImageView)findViewById(R.id.imageViewStar);
//		tapView.setImageResource(R.drawable.star_off);

		//質問uidの取得
		final String qUid = mQuestion.getQuestionUid();

		//表示
		// グローバル変数を扱うクラスを取得する
		common = (Common) getApplication();

		// Preferenceからuidを取得
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Set<String> set =  sp.getStringSet("favSet", new HashSet<String>());
		Iterator<String> it = set.iterator();

		// ログイン済みのユーザーを収録する
		FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

		// ログインしていなければお気に入り画像の表示をしない
		if (user == null) {

		} else {

//		}
			tapView.setImageResource(R.drawable.star_off);
			favOn = false;
			while (it.hasNext()) {
				if (it.next().equals(qUid)) {
					tapView.setImageResource(R.drawable.star_on);
					favOn = true;
				}
			}

			tapView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (favOn == false) {
						tapView.setImageResource(R.drawable.star_on);
						saveFav(qUid);
						favOn = true;
					} else {
						tapView.setImageResource(R.drawable.star_off);
						removeFav(qUid);
						favOn = false;
					}
				}

			});

		}

	}

	//課題
	private void saveFav(String fav) {
		// Preferenceに保存する
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sp.edit();
		common.stringHashSet.add(fav);
		Log.d("ログ追加", String.valueOf(common.stringHashSet.size()));
		editor.putStringSet("favSet", common.stringHashSet);
		editor.commit();
	}

	private void removeFav(String fav) {
		// Preferenceから削除する
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sp.edit();

		common.stringHashSet.remove(fav);
		Log.d("ログ削除", String.valueOf(common.stringHashSet.size()));
		editor.putStringSet("favSet", common.stringHashSet);
		editor.commit();
	}

}

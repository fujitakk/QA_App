package jp.techacademy.kaori.fujita.qa_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


public class FavoriteListActivity extends AppCompatActivity {

	private DatabaseReference mDatabaseReference;
	private DatabaseReference mDatabaseReference1;
	private DatabaseReference mDatabaseReference2;
	private DatabaseReference mDatabaseReference3;
	private DatabaseReference mDatabaseReference4;
	private ArrayList<String> mQuestionArrayListFav;
	private FavoriteListAdapter mfAdapter;
	private ListView mListView;
	private ArrayList<Question> mQuestionArrayList;
	private Common common;      // グローバル変数を扱うクラス
	private int mGenre = 0;

	private ChildEventListener mEventListener = new ChildEventListener() {
		@Override
		public void onChildAdded(DataSnapshot dataSnapshot, String s) {
			HashMap map = (HashMap) dataSnapshot.getValue();
			String title = (String) map.get("title");
			String body = (String) map.get("body");
			String name = (String) map.get("name");
			String uid = (String) map.get("uid");
			String imageString = (String) map.get("image");
			Bitmap image = null;
			byte[] bytes;
			if (imageString != null) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				bytes = Base64.decode(imageString, Base64.DEFAULT);
			} else {
				bytes = new byte[0];
			}
			String genre = (String) map.get("genre");

			ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
			HashMap answerMap = (HashMap) map.get("answers");
			if (answerMap != null) {
				for (Object key : answerMap.keySet()) {
					HashMap temp = (HashMap) answerMap.get((String) key);
					String answerBody = (String) temp.get("body");
					String answerName = (String) temp.get("name");
					String answerUid = (String) temp.get("uid");
					Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
					answerArrayList.add(answer);
				}
			}

			//fujita "mGenre"の値が適切でない
			Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), Integer.parseInt(genre), bytes, answerArrayList);

			for(int f = 0; f < mQuestionArrayListFav.size(); f++ ) {
//				Log.d("ログ ArrayListFav.get(f)", mQuestionArrayListFav.get(f));
				if(mQuestionArrayListFav.get(f).equals(dataSnapshot.getKey())) {
					mQuestionArrayList.add(question);
				}
			}
			mfAdapter.notifyDataSetChanged();
		}

		@Override
		public void onChildChanged(DataSnapshot dataSnapshot, String s) {
			HashMap map = (HashMap) dataSnapshot.getValue();

			// 変更があったQuestionを探す
			for (Question question: mQuestionArrayList) {
				if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
					// このアプリで変更がある可能性があるのは回答(Answer)のみ
					question.getAnswers().clear();
					HashMap answerMap = (HashMap) map.get("answers");
					if (answerMap != null) {
						for (Object key : answerMap.keySet()) {
							HashMap temp = (HashMap) answerMap.get((String) key);
							String answerBody = (String) temp.get("body");
							String answerName = (String) temp.get("name");
							String answerUid = (String) temp.get("uid");
							Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
							question.getAnswers().add(answer);
						}
					}

					mfAdapter.notifyDataSetChanged();
				}
			}
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
		setContentView(R.layout.activity_favorite_list);

		// UIの準備
		setTitle("お気に入り一覧");

		// ListViewの準備
		mListView = (ListView) findViewById(R.id.listViewFav);
		mfAdapter = new FavoriteListAdapter(this);
		mQuestionArrayList = new ArrayList<Question>();
		mfAdapter.notifyDataSetChanged();

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// Questionのインスタンスを渡して質問詳細画面を起動する
				Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
				intent.putExtra("question", mQuestionArrayList.get(position));
				startActivity(intent);
			}
		});

		// グローバル変数を扱うクラスを取得する
		common = (Common) getApplication();

		//preferenceよりお気に入りのuidを取得し、Arraylistに入れる
		mQuestionArrayListFav = new ArrayList<String>();
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		Set<String> set =  sp.getStringSet("favSet", new HashSet<String>());
		Iterator<String> it = set.iterator();
		while(it.hasNext()) {
			mQuestionArrayListFav.add( it.next().toString());
		}

		DatabaseReference dataBaseReference = FirebaseDatabase.getInstance().getReference();
		for(int q = 1; q <= 4; q++) {
			mGenre = q;
			mDatabaseReference = dataBaseReference.child(Const.ContentsPATH).child(String.valueOf(q));
			mDatabaseReference.addChildEventListener(mEventListener);
//			Log.d("ログ mDatabaseReference", "ログ");
		}

//		mQuestionArrayList.clear();
		mfAdapter.setFavoriteArrayList(mQuestionArrayList);
		mListView.setAdapter(mfAdapter);


	}


}

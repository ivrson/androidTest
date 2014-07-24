package com.cdol.androidtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.cdol.androidtest.bus.BusActivity;
import com.example.androidtest.R;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment implements OnClickListener {
		private EditText id;
		private EditText passwd;
		private ProgressDialog pDialog;
		private LinearLayout layout01;
		private Button loginBtn;
		private Button mapBtn;
		
		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			
			id = (EditText)rootView.findViewById(R.id.id);
			passwd=(EditText)rootView.findViewById(R.id.passwd);
			loginBtn = (Button)rootView.findViewById(R.id.loginButton);
			mapBtn = (Button)rootView.findViewById(R.id.mapButton);
			loginBtn.setOnClickListener(this);
			mapBtn.setOnClickListener(this);
			layout01 = (LinearLayout) rootView.findViewById(R.id.layout01);
			return rootView;
		}

		@Override
		public void onClick(View v) {
			int id = v.getId();
			
			switch (id) {
			case R.id.loginButton: loginProcess(); // 로그인 버튼이 클릭되면 로그인 처리를 시작한다.
			case R.id.mapButton: mapIntent();
			}
			
		}
		
		public void mapIntent(){
		//	Intent intentSubActivity = new Intent(getActivity(), MapActivity.class);
		//	startActivity(intentSubActivity);
		}
		// 네트웍 처리결과를 화면에 반영하기 위한 안드로이드 핸들러
		// responseHandler에 의해 처리된 결과가 success인 경우 바탕화면을 초록색으로 바꾸고
		// 로그인이 성공했다는 메시지를 토스트로 출력
		// 로그인이 실패한 경우 바탕화면을 빨강색으로 바꾸고 로그인실패 메시지를 토스트로 출력
		private final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pDialog.dismiss();
				String result = msg.getData().getString("RESULT");
				if (result.equals("success")) {
					Intent intentSubActivity = new Intent(getActivity(), BusActivity.class);
					startActivity(intentSubActivity);

				} else {
					Toast.makeText(getActivity(), "로그인 실패", Toast.LENGTH_LONG).show();
				}
			}
		};

		// 서버에서 전송된 XML 데이터를 파싱하기 위한 메서드
		// 이 예제에서는 서버에서 로그인이 성공하는 경우(id=kim&passwd=111)하는 경우
		// <result>success</result>
		// 실패하는 경우 <result>failed</result>를 반환하도록 설정해 두었다.
		public String parsingData(InputStream input) {
			String result = null;
			try {
				XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
				XmlPullParser parser = factory.newPullParser();
				parser.setInput(new InputStreamReader(input));
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					String name = parser.getName();
					if (name != null && name.equals("result"))
						result = parser.nextText();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return result;
		}
		
		// 로그인 버튼이 클릭되면 수행되는 메서드
		// responseHandler는 Http요청에 대한 HttpResponse가 반환되면 결과를 처리하기 위한
		// 콜백메서드를 정의하고 있는 객체이다.
		// Response를 받게 되면 parsingData()메서드를 호출하여 서버로 부터 받은 XML 파일을 처리하여
		// 그결과를 result 문자열로 반환받는다.
		// 이렇게 반환받은 result문자열을 화면에 반영하기위해 안드로이드UI핸들러인 handler를 통해 값을 전달한다.
		public void loginProcess() {
			final ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				@Override
				public String handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
					String result = null;
					HttpEntity entity = response.getEntity();
					Bundle bundle = new Bundle();
					Message message = handler.obtainMessage();
					
					result = parsingData(entity.getContent());
					
					if (result.equals("success"))
						bundle.putString("RESULT", "success");
					else
						bundle.putString("RESULT", "failed");
					
					message.setData(bundle);
					handler.sendMessage(message);
					
					return result;
				}
			};

			// 로그인이 처리되고 있다는 다이얼로그를 화면에 표시한다.
			pDialog = ProgressDialog.show(getActivity(), "", "로그인 처리중....");

			new Thread() {
				@Override
				public void run() {
					String url = "http://cdol.iptime.org:10080/androidTest/loginPage.do";
					HttpClient http = new DefaultHttpClient();
					try {
						// 서버에 전달할 파라메터 세팅
						ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("id", id.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("passwd", passwd.getText().toString()));
						
						// 응답시간이 5초가 넘으면 timeout 처리하려면 아래 코드의 커맨트를 풀고 실행한다.
						HttpParams params = http.getParams();
						HttpConnectionParams.setConnectionTimeout(params, 5000);
						HttpConnectionParams.setSoTimeout(params, 5000);

						// HTTP를 통해 서버에 요청을 전달한다.
						// 요청에 대한결과는 responseHandler의 handleResponse()메서드가 호출되어
						// 처리한다.
						// 서버에 전달되는 파라메터값을 인코딩하기위해 UrlEncodedFormEntity() 메서드를 사용한다.
						HttpPost httpPost = new HttpPost(url);
						UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
						httpPost.setEntity(entityRequest);
						http.execute(httpPost, responseHandler);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start(); // 스레드를 실행시킨다.
		}
	}

}

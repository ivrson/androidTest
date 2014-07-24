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
			case R.id.loginButton: loginProcess(); // �α��� ��ư�� Ŭ���Ǹ� �α��� ó���� �����Ѵ�.
			case R.id.mapButton: mapIntent();
			}
			
		}
		
		public void mapIntent(){
		//	Intent intentSubActivity = new Intent(getActivity(), MapActivity.class);
		//	startActivity(intentSubActivity);
		}
		// ��Ʈ�� ó������� ȭ�鿡 �ݿ��ϱ� ���� �ȵ���̵� �ڵ鷯
		// responseHandler�� ���� ó���� ����� success�� ��� ����ȭ���� �ʷϻ����� �ٲٰ�
		// �α����� �����ߴٴ� �޽����� �佺Ʈ�� ���
		// �α����� ������ ��� ����ȭ���� ���������� �ٲٰ� �α��ν��� �޽����� �佺Ʈ�� ���
		private final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				pDialog.dismiss();
				String result = msg.getData().getString("RESULT");
				if (result.equals("success")) {
					Intent intentSubActivity = new Intent(getActivity(), BusActivity.class);
					startActivity(intentSubActivity);

				} else {
					Toast.makeText(getActivity(), "�α��� ����", Toast.LENGTH_LONG).show();
				}
			}
		};

		// �������� ���۵� XML �����͸� �Ľ��ϱ� ���� �޼���
		// �� ���������� �������� �α����� �����ϴ� ���(id=kim&passwd=111)�ϴ� ���
		// <result>success</result>
		// �����ϴ� ��� <result>failed</result>�� ��ȯ�ϵ��� ������ �ξ���.
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
		
		// �α��� ��ư�� Ŭ���Ǹ� ����Ǵ� �޼���
		// responseHandler�� Http��û�� ���� HttpResponse�� ��ȯ�Ǹ� ����� ó���ϱ� ����
		// �ݹ�޼��带 �����ϰ� �ִ� ��ü�̴�.
		// Response�� �ް� �Ǹ� parsingData()�޼��带 ȣ���Ͽ� ������ ���� ���� XML ������ ó���Ͽ�
		// �װ���� result ���ڿ��� ��ȯ�޴´�.
		// �̷��� ��ȯ���� result���ڿ��� ȭ�鿡 �ݿ��ϱ����� �ȵ���̵�UI�ڵ鷯�� handler�� ���� ���� �����Ѵ�.
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

			// �α����� ó���ǰ� �ִٴ� ���̾�α׸� ȭ�鿡 ǥ���Ѵ�.
			pDialog = ProgressDialog.show(getActivity(), "", "�α��� ó����....");

			new Thread() {
				@Override
				public void run() {
					String url = "http://cdol.iptime.org:10080/androidTest/loginPage.do";
					HttpClient http = new DefaultHttpClient();
					try {
						// ������ ������ �Ķ���� ����
						ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
						nameValuePairs.add(new BasicNameValuePair("id", id.getText().toString()));
						nameValuePairs.add(new BasicNameValuePair("passwd", passwd.getText().toString()));
						
						// ����ð��� 5�ʰ� ������ timeout ó���Ϸ��� �Ʒ� �ڵ��� Ŀ��Ʈ�� Ǯ�� �����Ѵ�.
						HttpParams params = http.getParams();
						HttpConnectionParams.setConnectionTimeout(params, 5000);
						HttpConnectionParams.setSoTimeout(params, 5000);

						// HTTP�� ���� ������ ��û�� �����Ѵ�.
						// ��û�� ���Ѱ���� responseHandler�� handleResponse()�޼��尡 ȣ��Ǿ�
						// ó���Ѵ�.
						// ������ ���޵Ǵ� �Ķ���Ͱ��� ���ڵ��ϱ����� UrlEncodedFormEntity() �޼��带 ����Ѵ�.
						HttpPost httpPost = new HttpPost(url);
						UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(nameValuePairs, "UTF-8");
						httpPost.setEntity(entityRequest);
						http.execute(httpPost, responseHandler);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start(); // �����带 �����Ų��.
		}
	}

}

package org.python.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.Selection;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnPopulateContextMenuListener;
import android.widget.EditText;

//class ShellEditer extends EditText {
//	private Rect mRect;
//
//	private Paint mPaint;
//
//	// we need this constructor for ViewInflate
//	public ShellEditer(Context context, AttributeSet attrs, Map params) {
//		super(context, attrs, params);
//		this.initialize();
//		mRect = new Rect();
//		mPaint = new Paint();
//		mPaint.setStyle(Paint.Style.STROKE);
//		mPaint.setColor(0xFF0000FF);
//	}
//
//	@Override
//	protected void onDraw(Canvas canvas) {
//
//		int count = getLineCount();
//		Rect r = mRect;
//		Paint paint = mPaint;
//
//		for (int i = 0; i < count; i++) {
//			int baseline = getLineBounds(i, r);
//
//			canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
//		}
//
//		super.onDraw(canvas);
//	}
//
//	public String readEndLine() {
//		Editable t = this.getText();
//		InputMethod im = this.getInputMethod();
//		return null;
//	}
//
//	public void setOnKeyListener() {
//
//	}
//
//	private void initialize() {
//		this.setGravity(0x30);
//		this.setClickable(false);
//		this.setTextKeepState("fuck");
//		this.setTextSize(12);
//
//		//set the Listeners
//		OnClickListener ocl1 = null;
//		this.setOnClickListener(ocl1);
//		OnFocusChangeListener ocl2 = null;
//		this.setOnFocusChangeListener(ocl2);
//		OnLongClickListener ocl3 = null;
//		this.setOnLongClickListener(ocl3);
//		OnPopulateContextMenuListener ocml = null;
//		this.setOnPopulateContextMenuListener(ocml);
//	}
//
//	public void setOnClickListener(OnClickListener ocl) {
//		ocl = new OnClickListener() {
//			public void onClick(View v) {
//				v.computeScroll();
//				v.getBaseline();
//				EditText shell = (EditText) v;
//
//			}
//
//		};
//		super.setOnClickListener(ocl);
//	}
//
//	public void setOnFocusChangeListener(OnFocusChangeListener ocl) {
//		ocl = new OnFocusChangeListener() {
//
//			public void onFocusChanged(View v, boolean b) {
//
//			}
//
//		};
//		super.setOnFocusChangeListener(ocl);
//	}
//
//	public void setOnLongClickListener(OnLongClickListener ocl) {
//		ocl = new OnLongClickListener() {
//
//			public boolean onLongClick(View v) {
//				return false;
//			}
//
//		};
//		super.setOnLongClickListener(ocl);
//	}
//
//	public void setOnPopulateContextMenuListener(
//			OnPopulateContextMenuListener ocml) {
//		ocml = new OnPopulateContextMenuListener() {
//
//			public void onPopulateContextMenu(ContextMenu cm, View v, Object o) {
//
//			}
//
//		};
//		super.setOnPopulateContextMenuListener(ocml);
//	}
//
//}

/**
 * 
 * @author fuzhiqin
 * 
 */
public class Jythonroid extends Activity {
	ShellClient shellclient = null;

	private String ps1 = ">>> ";

	private String ps2 = "... ";

	private EditText shell = null;

	//	private ShellEditer shell=null;

	class ShellClient implements Runnable {
		private String address = "127.0.0.1";

		private int port = 6000;

		private InputStream is = null;

		private OutputStream os = null;

		private boolean keeponrun = true;

		private Jythonroid jr = null;

		private BufferedReader br = null;

		private PrintWriter pw = null;

		private Handler hd = null;

		public ShellClient(Jythonroid jythonroid, Handler hd) {
			this.jr = jythonroid;
			this.hd = hd;
			//wait till connected to the server
			Socket s = null;
			do {
				try {
					s = new Socket(address, port);
				} catch (UnknownHostException e1) {
				} catch (IOException e1) {
				}
				//FIXME sending some message here
				//callHandler();
			} while (s == null);
			callHandler("initial", true);
			try {
				is = s.getInputStream();
				os = s.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			br = new BufferedReader(new InputStreamReader(is));
			pw = new PrintWriter(new OutputStreamWriter(os));
		}

		public void run() {
				//is initialized
			try {
				while (keeponrun) {
					String result = br.readLine();
					callHandler("result", result);
				}
			} catch (IOException e) {
			}
		}

		private void callHandler(String key, String str) {
			Message msg = Message.obtain();
			HashMap<String, String> data = new HashMap<String, String>();
			data.put(key, str);
			msg.setData(data);
			hd.sendMessage(msg);
		}

		private void callHandler(String key, Boolean boo) {
			Message msg = Message.obtain();
			HashMap<String, Boolean> data = new HashMap<String, Boolean>();
			data.put(key, boo);
			msg.setData(data);
			hd.sendMessage(msg);
		}

		public void sendMsg(String msg) {
			pw.println(msg);
			pw.flush();
		}
	}

	/**
	 * provide an interactive shell in the screen
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);
		shell = (EditText) findViewById(R.id.shell);
		//		shell=new ShellEditer(this, null, null);
		setContentView(shell);
		shell.setEnabled(false);
		initializeShell(shell);
		Handler hd = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.getData().containsKey("initial")) {
					alert("initialized");
					shell.setEnabled(true);
				} else {
					shell.append("\n"+(String) msg.getData().get("result"));
				}
			}
		};
		//running the backend
		new Thread(new Runnable() {
			public void run() {
				try {
					Runtime.getRuntime().exec(
							"dalvikvm -classpath "
									+ "/data/app/Jythonroid.apk "
									+ "org.python.util.JythonServer");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, "JythonServer").start();
		shellclient = new ShellClient(this, hd);
		new Thread(shellclient).start();
	}

	public void println(String line) {

	}

	/**
	 * initialize the workplace widget
	 * 
	 * @param EditText shell
	 * 				initialize this EditText widget, adding some action listeners.
	 */
	private int linenumber = 0;

	private int position = 3;
	
	private String message="";
	
	private int deep=0;
	
	private void initializeShell(EditText shell) {
		final StringBuffer sb=new StringBuffer();
		//FIXME set the cursor to the end of '>>>'
		// cursorToEnd(shell);
//		Spanned str = Html.fromHtml("<font color='#FF0000'>sdaf</font>");
//		shell.append(str);
		shell.setSelection(3);
		shell.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int i, KeyEvent k) {
				EditText shell = (EditText) v;
				switch (i) {
				case 64:
					if (k.getAction()==1) {
						shell.setSelection(shell.getText().length());
						String[] f=shell.getText().toString().split("\n");
						String msg=f[f.length-1].substring(3);
						//check if end with ":"
						if(msg.endsWith(":")){
							if(deep==0){
								deep++;
								shell.append("\n . . . ");
								message=message+msg;
							}else if(deep>0){
								deep++;
								shell.append("\n . . . ");
								msg=msg.substring(4);
								message=message+msg;
							}
						}else{
							if(deep!=0){
								shell.append("\n . . . ");
								msg=msg.substring(3);
								message=message+msg;
								String head="";
								for(int j=0;j<deep;j++){
									head+="\t";
								}
								if(msg.startsWith(head)){
									alert(msg);
								}else{
									shellclient.sendMsg(message);
									message="";
									deep=0;
								}
								if(deep==0){
									shellclient.sendMsg(message);
									message="";
									deep=0;
								}
							}else{							
								message=message+msg;
								shellclient.sendMsg(message);
								message="";
								deep=0;
							}

							
						}
						
						
					}
					return true;
//				case 65:
//					//backspace key
//					if(sb.length()!=0){
//						sb.deleteCharAt(sb.length()-1);
//					}
//					return false;
//				case 21:
//					//move left <-
//					return false;
//				case 22:
//					//move right ->
//					return false;
				case 19:
					return true;
				case 20:
					return true;
				default:
//					if(i>=29&&i<=54){
//						sb.append(String.valueOf(i));
//					}
					return false;
				}
			}

		});
		shell.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				v.computeScroll();
				v.getBaseline();
				EditText shell = (EditText) v;

			}

		});
		shell.setOnFocusChangeListener(new OnFocusChangeListener() {

			public void onFocusChanged(View v, boolean b) {

			}

		});
		shell.setOnLongClickListener(new OnLongClickListener() {

			public boolean onLongClick(View v) {
				return false;
			}

		});
		shell
				.setOnPopulateContextMenuListener(new OnPopulateContextMenuListener() {

					public void onPopulateContextMenu(ContextMenu cm, View v,
							Object o) {

					}

				});
	}

	public void cursorToEnd(EditText shell) {
		Editable etext = shell.getText();
		int position = etext.length();
		// end of buffer, for instance
		Selection.setSelection(etext, position);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, R.string.setting, R.drawable.icon);
		menu.add(0, 1, R.string.sure_en);
		menu.add(0, 2, R.string.app_name);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Menu.Item item) {
		switch (item.getId()) {
		case 0:
			alert("fuckyou");
			return true;
		case 1:
			alert("iloveyou");
			return true;
		case 2:
			alert("comeonbaby");
			return true;
		}
		return false;
	}

	/**
	 * alert a string message on the screen
	 * 
	 * @param msg
	 */
	private void alert(CharSequence msg) {
		new AlertDialog.Builder(this).setIcon(R.drawable.icon).setTitle(msg)
				.setPositiveButton(R.string.sure_cn,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

							}
						}).setCancelable(true).setMessage("Hey").show();
	}

}

class JythonroidExcepiton extends Exception {

}

/*Copyright ©2016 TommyLemon(https://github.com/TommyLemon)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package apijson.demo.client.base;

import zuo.biao.library.base.BaseBroadcastReceiver;
import zuo.biao.library.util.Log;
import zuo.biao.library.util.StringUtil;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import apijson.demo.client.R;
import apijson.demo.client.activity_fragment.LoginActivity;
import apijson.demo.client.application.APIJSONApplication;
import apijson.demo.client.model.User;
import apijson.demo.client.util.ActionUtil;

public abstract class BaseListFragment<T, LV extends AbsListView, BA extends BaseAdapter>
extends zuo.biao.library.base.BaseListFragment<T, LV, BA> implements Runnable {
	protected static final String TAG = "BaseListFragment";
	
	
	protected User currentUser;
	protected long currentUserId;
	protected boolean isLoggedIn;
	@Override
	@Nullable
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		setCurrentUser();
		
		BaseBroadcastReceiver.register(context, receiver, ActionUtil.ACTION_USER_CHANGED);
		
		return view;
	}

	private void setCurrentUser() {
		currentUser = APIJSONApplication.getInstance().getCurrentUser();
		currentUserId = currentUser == null ? 0 : currentUser.getId();
		isLoggedIn = isCurrentUserCorrect();
	}

	protected static boolean isCurrentUser(long userId) {
		return APIJSONApplication.getInstance().isCurrentUser(userId);
	}
	/**未登录会toLoginActivity();
	 * @return isLoggedIn
	 */
	protected boolean verifyLogin() {
		if (isLoggedIn == false) {
			showShortToast("请先登录");
			toLoginActivity();
		}
		return isLoggedIn;
	}
	protected void toLoginActivity() {
		startActivity(LoginActivity.createIntent(context));
		context.overridePendingTransition(R.anim.bottom_push_in, R.anim.hold);
	}

	@Override
	public void initData() {
		super.initData();
		loadAfterCorrect();
	}
	
	/*只有当isCurrentUserCorrect()时才会被调用，如果不符合则会获取currentUser并再次判断来决定是否调用
	 */
	@Override
	public abstract void run();
	
	private boolean isDataChanged = false;
	/**
	 */
	protected void invalidate() {
		if (isRunning() == false) {
			isDataChanged = true;
			Log.w(TAG, "invalidate  isRunning() == false >> return;");
			return;
		}
		isDataChanged = false;
		
		setCurrentUser();
		loadAfterCorrect();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (isDataChanged) {
			Log.d(TAG, "onResume  isDataChanged >> invalidate();");
			invalidate();
		}
	}

	/**
	 * @param runnable
	 */
	protected void loadAfterCorrect() {
		if (isCurrentUserCorrect() == false) {//请求currentUser都统一交给MainTabActivity，避免同时多次相同请求
			Log.e(TAG, "loadAfterCorrect  isCurrentUserCorrect() == false >> return;");
			return;
		}
		run();
	}


	/**
	 * @return
	 */
	public boolean isCurrentUserCorrect() {
		return isUserCorrect(currentUser);
	}
	/**
	 * @param user
	 * @return
	 */
	public boolean isUserCorrect(User user) {
		return user != null && user.getId() > 0;
	}


	@Override
	public void onDestroy() {
		BaseBroadcastReceiver.unregister(context, receiver);
		super.onDestroy();
	}

	
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent == null ? null : intent.getAction();
			if (isAlive() == false || StringUtil.isNotEmpty(action, true) == false) {
				Log.e(TAG, "receiver.onReceive  isAlive() == false" +
						" || StringUtil.isNotEmpty(action, true) == false >> return;");
				return;
			}

			if (ActionUtil.ACTION_USER_CHANGED.equals(action)) {
				if (isCurrentUser(intent.getLongExtra(INTENT_ID, 0))) {
					invalidate();
				}
			}
		}
	};

}
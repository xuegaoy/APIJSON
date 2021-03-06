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

package apijson.demo.client.adapter;

import java.util.List;

import zuo.biao.library.base.BaseView;
import zuo.biao.library.base.BaseViewAdapter;
import zuo.biao.library.util.ImageLoaderUtil;
import zuo.biao.library.util.StringUtil;
import zuo.biao.library.util.TimeUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import apijson.demo.client.R;
import apijson.demo.client.activity_fragment.UserActivity;
import apijson.demo.client.adapter.CommentAdapter.ItemView;
import apijson.demo.client.adapter.CommentAdapter.ItemView.OnCommentClickListener;
import apijson.demo.client.model.CommentItem;

/**评论列表
 * @author Lemon
 */
public class CommentAdapter extends BaseViewAdapter<CommentItem, ItemView> {

	private OnCommentClickListener onCommentClickListener;
	public CommentAdapter(Activity context, OnCommentClickListener onCommentClickListener) {     
		super(context);
		this.onCommentClickListener = onCommentClickListener;
	}

	@Override
	public long getItemId(int position) {
		return getItem(position).getId();
	}

	private boolean showAll = false;
	public void setShowAll(boolean showAll) {
		this.showAll = showAll;
	}
	
	@Override
	public ItemView createView(int position, ViewGroup parent) {
		return new ItemView(context, resources, showAll).setOnCommentClickListener(onCommentClickListener);
	}


	public static class ItemView extends BaseView<CommentItem> implements OnClickListener {  

		/**
		 */
		public interface OnCommentClickListener {
			void onCommentClick(CommentItem item, int position, int index, boolean isLong);
		}
		
		private OnCommentClickListener onCommentClickListener;

		public ItemView setOnCommentClickListener(OnCommentClickListener onCommentClickListener) {
			this.onCommentClickListener = onCommentClickListener;
			return this;
		}
		
		
		private boolean showAll;
		public ItemView(Activity context, Resources resources, boolean showAll) {
			super(context, resources);
			this.showAll = showAll;
		}

		private LayoutInflater inflater;
		
		public ImageView ivCommentHead;
		public TextView tvCommentName;
		public TextView tvCommentContent;
		public TextView tvCommentTime;

		public LinearLayout llCommentContainer;
		public TextView tvCommentMore;
		
		@SuppressLint("InflateParams")
		@Override
		public View createView(LayoutInflater inflater) {
			this.inflater = inflater;
			convertView = inflater.inflate(R.layout.comment_main_item, null);

			ivCommentHead = findViewById(R.id.ivCommentHead, this);
			llCommentContainer = findViewById(R.id.llCommentContainer);
			tvCommentMore = findViewById(R.id.tvCommentMore);

			tvCommentName = (TextView) findViewById(R.id.tvCommentName, this);
			tvCommentContent = (TextView) findViewById(R.id.tvCommentContent);
			tvCommentTime = (TextView) findViewById(R.id.tvCommentTime);

			return convertView;
		}

		@Override
		public void bindView(CommentItem data){
			this.data = data;

			String name = StringUtil.getTrimedString(data.getUser().getName());
			String content = StringUtil.getTrimedString(data.getComment().getContent());

			tvCommentName.setText("" + name);
			tvCommentContent.setText("" + content);
			tvCommentTime.setText("" + TimeUtil.getSmartDate(data.getDate()));
			ImageLoaderUtil.loadImage(ivCommentHead, data.getUser().getHead(), ImageLoaderUtil.TYPE_OVAL);
			setChildComment(data, showAll);
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.tvCommentContent:
				if (onCommentClickListener != null) {
					onCommentClickListener.onCommentClick(data, position, -1, false);
				}
				break;
			case R.id.ivCommentHead:
			case R.id.tvCommentName:
				toActivity(UserActivity.createIntent(context, data.getUser().getId()));
				break;
			default:
				break;
			}
		}
		
		/**显示子评论
		 * @param data
		 */
		@SuppressLint("InflateParams")
		public void setChildComment(final CommentItem parentItem, boolean showAll) {

			List<CommentItem> downList = parentItem.getChildList();
			if (downList == null || downList.isEmpty()) {
				llCommentContainer.removeAllViews();
				findViewById(R.id.vCommentItemDivider).setVisibility(View.GONE);
			} else {
				findViewById(R.id.vCommentItemDivider).setVisibility(View.VISIBLE);

				tvCommentMore.setVisibility(View.GONE);
				if (showAll == false && downList.size() > 3) {
					tvCommentMore.setText("查看更多");
					tvCommentMore.setVisibility(View.VISIBLE);
					tvCommentMore.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							setChildComment(parentItem, true);
						}
					});

					downList = downList.subList(0, 3);
				}

				llCommentContainer.removeAllViews();
				for (int i = 0; i < downList.size(); i++) {
					final int index = i;
					
					TextView childComment = (TextView) inflater.inflate(R.layout.comment_down_item, null);
					
					final CommentItem data = downList.get(i);
					String name = StringUtil.getTrimedString(data.getUser().getName());
					String content = StringUtil.getTrimedString(data.getComment().getContent());
					childComment.setText(Html.fromHtml("<font color=\"#25a281\">" + StringUtil.getString(name) + "</font>"
							+ " 回复 " + "<font color=\"#25a281\">" + StringUtil.getString(data.getToUser().getName())
							+ "</font>" + " : " + content));

					childComment.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							onCommentClick(data, position, index, false);
						}
					});
					childComment.setOnLongClickListener(new OnLongClickListener() {
						
						@Override
						public boolean onLongClick(View v) {
							onCommentClick(data, position, index, true);
							return true;
						}
					});

					llCommentContainer.addView(childComment);
				}
			}
		}

		protected void onCommentClick(CommentItem item, int position, int index, boolean isLong) {
			if (onCommentClickListener != null) {
				onCommentClickListener.onCommentClick(item, position, index, isLong);
			}
		}
	}

}

package com.imooc.imooc_voice.view.discory.square.detail;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.imooc.imooc_voice.R;
import com.imooc.imooc_voice.util.AnimUtil;
import com.imooc.imooc_voice.util.TimeUtil;
import com.imooc.imooc_voice.view.user.UserDetailDelegate;
import com.imooc.lib_api.RequestCenter;
import com.imooc.lib_api.model.playlist.PlayListCommentEntity;
import com.imooc.lib_api.model.song.CommentLikeBean;
import com.imooc.lib_api.model.song.MusicCommentBean;
import com.imooc.lib_api.model.song.PlayListCommentBean;
import com.imooc.lib_common_ui.delegate.NeteaseDelegate;
import com.imooc.lib_common_ui.delegate.NeteaseLoadingDelegate;
import com.imooc.lib_image_loader.app.ImageLoaderManager;
import com.imooc.lib_network.listener.DisposeDataListener;

import java.util.ArrayList;
import java.util.List;

import static com.imooc.lib_common_ui.utils.Constants.ALBUM;
import static com.imooc.lib_common_ui.utils.Constants.PLAYLIST;
import static com.imooc.lib_common_ui.utils.Constants.SONG;


public class CommentDelegate extends NeteaseLoadingDelegate implements View.OnClickListener {

	private TextView mTvHeaderTitle;
	private TextView mTvHeaderCreator;
	private TextView mTvCommentTitle;
	private RelativeLayout mRlGedanHeader;
	private ImageView mBackView;
	private ImageView mIvAlbumRightIcon;
	private MultipleSectionGedanCommentAdapter mAdapter;

	private RecyclerView mRecyclerViewComment;

	private static final String ARGS_COMMENT_ID = "ARGS_COMMENT_ID";
	private static final String ARGS_COMMENT_TYPE = "ARGS_COMMENT_TYPE";
	private static final String ARGS_COMMENT_HEADER_IMG = "ARGS_COMMENT_HEADER_IMG";
	private static final String ARGS_COMMENT_HEADER_TOP = "ARGS_COMMENT_HEADER_TOP";
	private static final String ARGS_COMMENT_HEADER_BOTTOM = "ARGS_COMMENT_HEADER_BOTTOM";


	//评论类型 歌曲评论 歌单评论 专辑评论  0: 歌曲  1: mv 2: 歌单 3: 专辑 4: 电台 5: 视频 6: 动态

	//资源ID
	private String id;
	//评论类型
	private int type;
	//歌单图片
	private String headerImg;
	//歌单创建者
	private String headerTop;
	//歌单标题
	private String headerBottom;

	private ArrayList<PlayListCommentEntity> entities = new ArrayList<>();

	public static CommentDelegate newInstance(String id, int type, String img, String creator, String title) {
		final Bundle args = new Bundle();
		args.putString(ARGS_COMMENT_ID, id);
		args.putString(ARGS_COMMENT_HEADER_IMG, img);
		args.putString(ARGS_COMMENT_HEADER_BOTTOM, creator);
		args.putString(ARGS_COMMENT_HEADER_TOP, title);
		args.putInt(ARGS_COMMENT_TYPE, type);
		final CommentDelegate delegate = new CommentDelegate();
		delegate.setArguments(args);
		return delegate;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Bundle args = getArguments();
		if (args != null) {
			id = args.getString(ARGS_COMMENT_ID);
			headerImg = args.getString(ARGS_COMMENT_HEADER_IMG);
			headerTop = args.getString(ARGS_COMMENT_HEADER_TOP);
			headerBottom = args.getString(ARGS_COMMENT_HEADER_BOTTOM);
			type = args.getInt(ARGS_COMMENT_TYPE);
		}
	}


	@Override
	public void initView() {
		initCommentView();
	}

	@Override
	public int setLoadingViewLayout() {
		return R.layout.delegate_gedan_detail_comment;
	}


	@SuppressLint({"StaticFieldLeak", "SetTextI18n"})
	private void initCommentView() {

		mRecyclerViewComment = rootView.findViewById(R.id.rv_gedan_comment_normal);
		mTvHeaderTitle = rootView.findViewById(R.id.tv_gedan_detail_comment_title);
		mTvHeaderCreator = rootView.findViewById(R.id.tv_gedan_detail_comment_creator);
		mRlGedanHeader = rootView.findViewById(R.id.rl_gedan_comment_header);
		mBackView = rootView.findViewById(R.id.img_gedan_comment_back);
		mTvCommentTitle = rootView.findViewById(R.id.tv_gedan_detail_comment_num);
		mIvAlbumRightIcon = rootView.findViewById(R.id.iv_album_right_flag);


		mRlGedanHeader.setOnClickListener(this);
		mBackView.setOnClickListener(this);
		//设置歌手名 或用户名
		if (type == PLAYLIST) {
			//显示用户名
			String creator = "by " + headerBottom;
			SpannableString msp = new SpannableString(creator);
			ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan(Color.GRAY);
			msp.setSpan(foregroundColorSpan, 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
			mTvHeaderCreator.setText(msp);
		} else {
			//专辑显示右边的图片
			mTvHeaderCreator.setText(headerBottom);
			mIvAlbumRightIcon.setVisibility(View.VISIBLE);
		}
		//标题
		mTvHeaderTitle.setText(headerTop);

		ImageLoaderManager.getInstance().displayImageForCorner(rootView.findViewById(R.id.iv_gedan_detail_comment_img), headerImg, 5);

		//加载不同类型的评论
		switch (type) {
			case PLAYLIST:
				//歌单评论
				RequestCenter.getPlaylistComment(id, new DisposeDataListener() {
					@Override
					public void onSuccess(Object responseObj) {
						loadCommentList((PlayListCommentBean) responseObj, PLAYLIST);
					}

					@Override
					public void onFailure(Object reasonObj) {

					}
				});
				break;
			case SONG:
				//歌曲评论
				RequestCenter.getMusicComment(id, new DisposeDataListener() {
					@Override
					public void onSuccess(Object responseObj) {
						loadCommentList((PlayListCommentBean) responseObj, SONG);
					}

					@Override
					public void onFailure(Object reasonObj) {

					}
				});
				break;
			//专辑评论
			case ALBUM:
				RequestCenter.getAlbumComment(id, new DisposeDataListener() {
					@Override
					public void onSuccess(Object responseObj) {
						loadCommentList((PlayListCommentBean) responseObj, ALBUM);
					}

					@Override
					public void onFailure(Object reasonObj) {

					}
				});
				break;
			default:
				break;
		}

	}

	@SuppressLint("SetTextI18n")
	private void loadCommentList(PlayListCommentBean commentBean, int type) {
		//评论数量
		mTvCommentTitle.setText("评论(" + commentBean.getTotal() + ")");
		entities.add(new PlayListCommentEntity(true, "精彩评论", ""));
		if(commentBean.getHotComments() != null && commentBean.getHotComments().size() > 0){
			for (int i = 0; i < commentBean.getHotComments().size(); i++) {
				entities.add(new PlayListCommentEntity(commentBean.getHotComments().get(i)));
			}
		}
		entities.add(new PlayListCommentEntity(true, "最新评论", String.valueOf(commentBean.getTotal())));
		for (int j = 0; j < commentBean.getComments().size(); j++) {
			entities.add(new PlayListCommentEntity(commentBean.getComments().get(j)));
		}
		mAdapter = new MultipleSectionGedanCommentAdapter(id, type, getContext(), this, entities);
		mRecyclerViewComment.setAdapter(mAdapter);
		mRecyclerViewComment.setLayoutManager(new LinearLayoutManager(getContext()) {
			@Override
			public boolean canScrollVertically() {
				return false;
			}
		});
		addRootView();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.rl_gedan_comment_header:
			case R.id.img_gedan_comment_back:
				getSupportDelegate().pop();
				break;
			default:
				break;
		}
	}


	public static class MultipleSectionGedanCommentAdapter extends BaseSectionQuickAdapter<PlayListCommentEntity, BaseViewHolder> {

		private ImageLoaderManager manager;
		private NeteaseDelegate mDelegate;
		private String commentId;
		private Context mContext;
		private int commentType;

		//评论类型 tyep  0: 歌曲  1: mv 2: 歌单 3: 专辑 4: 电台 5: 视频 6: 动态
		public MultipleSectionGedanCommentAdapter(String id, int type, Context context, NeteaseDelegate delegate, List<PlayListCommentEntity> data) {
			super(R.layout.item_gedan_detail_comment, R.layout.item_gedan_comment_header, data);
			manager = ImageLoaderManager.getInstance();
			commentId = id;
			mContext = context;
			commentType = type;
			mDelegate = delegate;
		}

		@Override
		protected void convertHead(BaseViewHolder baseViewHolder, PlayListCommentEntity playListCommentEntity) {
			baseViewHolder.setText(R.id.tv_gedan_detail_comment_header_title, playListCommentEntity.header);
			baseViewHolder.setText(R.id.tv_gedan_detail_comment_header_count, playListCommentEntity.getCount());
		}

		@Override
		protected void convert(@NonNull final BaseViewHolder baseViewHolder, PlayListCommentEntity playListCommentEntity) {
			final MusicCommentBean.CommentsBean bean = playListCommentEntity.t;
			//用户头像
			ImageView avatarImg = baseViewHolder.getView(R.id.iv_item_gedan_comment_avatar_img);
			manager.displayImageForCircle(avatarImg, bean.getUser().getAvatarUrl());
			//用户昵称
			baseViewHolder.setText(R.id.tv_item_gedan_comment_avatar_name, bean.getUser().getNickname());
			//用户VIP类型
			if (bean.getUser().getVipType() == 11 && bean.getUser().getVipRights() != null) {
				if(bean.getUser().getVipRights().getRedVipAnnualCount() == 1){
					//年费vip TODO 年vip 图片
				}else{
					//普通vip
					baseViewHolder.setVisible(R.id.iv_item_gedan_comment_avatar_vip, true);
				}
			}
			//评论时间
			baseViewHolder.setText(R.id.tv_item_gedan_comment_time, TimeUtil.getTimeStandardOnlyYMDChinese(bean.getTime()));
			//点赞数量
			if (bean.getLikedCount() != 0) {
				baseViewHolder.setVisible(R.id.tv_item_gedan_comment_zan_count, true);
				baseViewHolder.setText(R.id.tv_item_gedan_comment_zan_count, String.valueOf(bean.getLikedCount()));
			}
			//评论内容
			baseViewHolder.setText(R.id.tv_item_gedan_comment_content, bean.getContent());
			//回复数量
			if (bean.getBeReplied().size() != 0) {
				baseViewHolder.setVisible(R.id.tv_item_gedan_comment_replied, true);
				baseViewHolder.setText(R.id.tv_item_gedan_comment_replied, bean.getBeReplied().size() + "条回复");
			}
			//点击头像和用户名进入用户详情
			baseViewHolder.setOnClickListener(R.id.iv_item_gedan_comment_avatar_img, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDelegate.getSupportDelegate().start(UserDetailDelegate.newInstance(String.valueOf(bean.getUser().getUserId())));
				}
			});
			baseViewHolder.setOnClickListener(R.id.tv_item_gedan_comment_avatar_name, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mDelegate.getSupportDelegate().start(UserDetailDelegate.newInstance(String.valueOf(bean.getUser().getUserId())));
				}
			});
			final ImageView praiseView = baseViewHolder.getView(R.id.iv_item_gedan_comment_zan);
			//tag : true 当前是赞 false当前不是赞
			if (bean.isLiked()) {
				//当前是赞过的
				praiseView.setTag(true);
				praiseView.setImageResource(R.drawable.ic_parise_red);
			} else {
				praiseView.setTag(false);
				praiseView.setImageResource(R.drawable.ic_parise);
			}
			baseViewHolder.setOnClickListener(R.id.iv_item_gedan_comment_zan, new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					//是否已经点过赞
					Boolean parise = (Boolean) praiseView.getTag();
					//点赞或取消点赞
					RequestCenter.getlikeComment(commentId, bean.getCommentId(), !parise, commentType, new DisposeDataListener() {
						@Override
						public void onSuccess(Object responseObj) {
							CommentLikeBean result = (CommentLikeBean) responseObj;
							if (result.getCode() == 200) {
								praiseView.setTag(!parise);
								if (!parise) {
									praiseView.setImageResource(R.drawable.ic_parise_red);
									//点赞
									AnimUtil.getLikeAnim(praiseView).start();
									baseViewHolder.setText(R.id.tv_item_gedan_comment_zan_count, String.valueOf(bean.getLikedCount() + 1));
									baseViewHolder.setTextColor(R.id.tv_item_gedan_comment_zan_count, Color.parseColor("#FF3A3A"));
									Toast.makeText(mContext, "点赞成功", Toast.LENGTH_SHORT).show();
								} else {
									baseViewHolder.setText(R.id.tv_item_gedan_comment_zan_count, String.valueOf(bean.getLikedCount()));
									praiseView.setImageResource(R.drawable.ic_parise);
									baseViewHolder.setTextColor(R.id.tv_item_gedan_comment_zan_count, Color.GRAY);
									Toast.makeText(mContext, "取消赞成功", Toast.LENGTH_SHORT).show();
								}

							} else {
								Toast.makeText(mContext, "点赞或取消赞失败", Toast.LENGTH_SHORT).show();
							}
						}

						@Override
						public void onFailure(Object reasonObj) {
							Toast.makeText(mContext, "点赞或取消赞失败", Toast.LENGTH_SHORT).show();
						}
					});

				}
			});
		}


	}
}

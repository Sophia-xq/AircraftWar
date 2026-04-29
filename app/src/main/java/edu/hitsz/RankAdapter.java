package edu.hitsz;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import edu.hitsz.dao.GameRecord;

public class RankAdapter extends BaseAdapter {

    private final Context context;
    private List<GameRecord> records;
    private final OnDeleteClickListener deleteListener;

    // 前三名徽章颜色
    private static final int COLOR_RANK1 = Color.parseColor("#FFD700"); // 金
    private static final int COLOR_RANK2 = Color.parseColor("#C0C0C0"); // 银
    private static final int COLOR_RANK3 = Color.parseColor("#CD7F32"); // 铜
    private static final int COLOR_OTHER = Color.parseColor("#444444"); // 普通

    // 前三名名字颜色
    private static final int TEXT_RANK1 = Color.parseColor("#FFD700");
    private static final int TEXT_RANK2 = Color.parseColor("#E0E0E0");
    private static final int TEXT_RANK3 = Color.parseColor("#CD7F32");
    private static final int TEXT_OTHER = Color.WHITE;

    public interface OnDeleteClickListener {
        void onDeleteClick(int position, GameRecord record);
    }

    public RankAdapter(Context context, List<GameRecord> records, OnDeleteClickListener listener) {
        this.context = context;
        this.records = records;
        this.deleteListener = listener;
    }

    @Override public int getCount() { return records.size(); }
    @Override public Object getItem(int pos) { return records.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    public void updateData(List<GameRecord> newRecords) {
        this.records = newRecords;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_rank, parent, false);
            holder = new ViewHolder();
            holder.tvRank       = convertView.findViewById(R.id.tv_rank);
            holder.tvName       = convertView.findViewById(R.id.tv_name);
            holder.tvScore      = convertView.findViewById(R.id.tv_score);
            holder.tvDifficulty = convertView.findViewById(R.id.tv_difficulty);
            holder.tvTime       = convertView.findViewById(R.id.tv_time);
            holder.btnDelete    = convertView.findViewById(R.id.btn_delete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GameRecord record = records.get(position);
        int rank = position + 1;

        // ── 排名徽章：前三名特殊颜色 + emoji ────────────────────────────
        String rankText;
        int badgeColor;
        int nameColor;

        switch (rank) {
            case 1:
                rankText = "🥇"; badgeColor = COLOR_RANK1; nameColor = TEXT_RANK1; break;
            case 2:
                rankText = "🥈"; badgeColor = COLOR_RANK2; nameColor = TEXT_RANK2; break;
            case 3:
                rankText = "🥉"; badgeColor = COLOR_RANK3; nameColor = TEXT_RANK3; break;
            default:
                rankText = String.valueOf(rank); badgeColor = COLOR_OTHER; nameColor = TEXT_OTHER; break;
        }

        holder.tvRank.setText(rankText);
        holder.tvName.setTextColor(nameColor);

        // 动态修改徽章背景颜色（GradientDrawable 是圆形 shape）
        GradientDrawable badge = (GradientDrawable) context.getDrawable(R.drawable.rank_badge_bg);
        if (badge != null) {
            badge = (GradientDrawable) badge.mutate();
            badge.setColor(badgeColor);
            holder.tvRank.setBackground(badge);
        }

        // ── 数据绑定 ──────────────────────────────────────────────────
        holder.tvName.setText(record.getPlayerName());
        holder.tvScore.setText(String.valueOf(record.getScore()));
        holder.tvDifficulty.setText(getDifficultyDisplay(record.getDifficulty()));
        holder.tvTime.setText(record.getFormattedTime());

        // ── 删除按钮 ──────────────────────────────────────────────────
        holder.btnDelete.setOnClickListener(v ->
                new AlertDialog.Builder(context)
                        .setTitle("确认删除")
                        .setMessage("确定删除该记录吗？")
                        .setPositiveButton("删除", (d, w) -> {
                            if (deleteListener != null)
                                deleteListener.onDeleteClick(position, record);
                        })
                        .setNegativeButton("取消", null)
                        .show()
        );

        return convertView;
    }

    private String getDifficultyDisplay(String d) {
        switch (d) {
            case "easy":   return "简单";
            case "normal": return "普通";
            case "hard":   return "困难";
            default:       return d;
        }
    }

    static class ViewHolder {
        TextView tvRank, tvName, tvScore, tvDifficulty, tvTime;
        Button btnDelete;
    }
}
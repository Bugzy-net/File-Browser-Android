package net.bugzy.filebrowser;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by OMohamed on 2/16/2017.
 */

public class FileSelectionAdapter extends RecyclerView.Adapter<FileSelectionAdapter.FileSelectionViewHolder> {

    private String[] extensionsToAccept;
    private List<File> files;
    private File parentFolder;
    private LayoutInflater layoutInflater;
    private FileSelectionListener fileSelectionListener;
//    private boolean checkable;

    public FileSelectionAdapter(@NonNull FileSelectionListener fileSelectionListener, @NonNull String[] extensionsToAccept,
                                @Nullable String rootFolder) {
        layoutInflater = LayoutInflater.from(fileSelectionListener.getContext());
        this.extensionsToAccept = extensionsToAccept;
        parentFolder = (rootFolder != null) ? new File(rootFolder) : Environment.getExternalStorageDirectory();
        this.fileSelectionListener = fileSelectionListener;
        updateFiles();
    }

    @Override
    public FileSelectionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FileSelectionViewHolder(layoutInflater.inflate(R.layout.layout_file_item, parent, false));
    }

    @Override
    public void onBindViewHolder(FileSelectionViewHolder holder, int position) {
        if (canGoUp() && position == 0) {
//            holder.checkBox.setVisibility(View.INVISIBLE);
            holder.fileName.setVisibility(View.VISIBLE);
            holder.fileThumbnail.setImageResource(R.drawable.ic_folder_24px);
            holder.fileName.setText("\\..");
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goUp();
                }
            });
        } else {
            int positionInList = canGoUp() ? position - 1 : position;
            File file = files.get(positionInList);
            if (file.isDirectory()) {
                holder.fileThumbnail.setImageResource(R.drawable.ic_folder_24px);
                holder.fileName.setText("..\\" + file.getName());
                holder.itemView.setTag(file);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentFolder = (File) v.getTag();
                        updateFiles();
                    }
                });
            } else {
                holder.fileThumbnail.setImageResource(R.drawable.ic_insert_drive_file_24px);
                holder.fileName.setText(file.getName());
                holder.itemView.setTag(file);
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        fileSelectionListener.onFileSelected((File) v.getTag());
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        if (canGoUp()) {
            return files.size() + 1;
        } else {
            return files.size();
        }
    }

    private void updateFiles() {
        fileSelectionListener.onFolderSelected(parentFolder);
        files = getListFiles(parentFolder);
        notifyDataSetChanged();
    }

    private List<File> getListFiles(File parentDir) {
        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDir.listFiles(fileFilter);
        inFiles.addAll(Arrays.asList(files));
        Collections.sort(inFiles, fileComparator);
        return inFiles;
    }

    public boolean canGoUp() {
        return !parentFolder.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath());
    }

    public void goUp() {
        parentFolder = parentFolder.getParentFile();
        updateFiles();
    }

    private Comparator<File> fileComparator = new Comparator<File>() {
        @Override
        public int compare(File o1, File o2) {
            if (o1.isDirectory() && !o2.isDirectory()) {
                return -1;
            } else if (!o1.isDirectory() && o2.isDirectory()) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
    };

    public File getCurrentPath() {
        return parentFolder;
    }

    private FileFilter fileFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()) {
                return true;
            } else {
                String filenameArray[] = pathname.getName().split("\\.");
                if (filenameArray.length > 1) {
                    String extension = filenameArray[filenameArray.length - 1];
                    for (String ext : extensionsToAccept) {
                        if (extension.equalsIgnoreCase(ext)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    };

    class FileSelectionViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView fileThumbnail;
        TextView fileName;
//        AppCompatCheckBox checkBox;

        FileSelectionViewHolder(View itemView) {
            super(itemView);
            fileThumbnail = itemView.findViewById(R.id.file_thumbnail);
            fileName = itemView.findViewById(R.id.file_name);
//            checkBox = itemView.findViewById(R.id.checkbox);
        }
    }



}

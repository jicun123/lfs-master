package cn.lxinet.lfs.vo;

import jakarta.servlet.http.HttpServletRequest;

import java.io.Serializable;
import java.util.List;

public class Page<T> implements Serializable {
    private List<T> list;
    private int pageNo = 1;
    private int pageSize = 10;
    private int totalPage;
    private int totalCount;
    private boolean isFirstPage = false;
    private boolean isLastPage = false;
    private HttpServletRequest request;

    public Page(int pageNo, int pageSize) {
        this.setPageNo(pageNo);
        this.setPageSize(pageSize);
    }

    public Page(List<T> list, int pageNo, int pageSize, int totalCount) {
        this.setList(list);
        this.setPageNo(pageNo);
        this.setPageSize(pageSize);
        this.setTotalCount(totalCount);
    }

    public Page(HttpServletRequest request){
        this.request = request;
        String no = request.getParameter("pageNo");
        try {
            this.setPageNo(Integer.parseInt(no));
            if(this.getPageNo() < 1){
                this.setPageNo(1);
            }
        }catch (Exception e){
            this.setPageNo(1);
        }
        String size = request.getParameter("pageSize");
        try {
            this.setPageSize(Integer.parseInt(size));
        }catch (Exception e){
            this.setPageSize(20);
        }
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        if(pageNo < 1){
            pageNo = 1;
        }
        if(pageNo == 1){
            isFirstPage = true;
        }else {
            isFirstPage = false;
        }
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage() {
        this.totalPage = (this.totalCount - 1) / this.pageSize + 1;
        if(this.getPageNo() == this.getTotalPage()){
            isLastPage = true;
        }else {
            isLastPage = false;
        }
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
        this.setTotalPage();
    }

    public boolean isFirstPage() {
        return pageNo == 1;
    }

    public boolean isLastPage() {
        return pageNo == totalPage;
    }

    public int getStartRow() {
        return (this.pageNo - 1) * pageSize;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}

package cn.edu.bnu.demo_collection;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

/**
 * @CreateDate: 2025/7/1
 * @author: jingjie
 * @desc:
 */
public interface Api {
    /**
     * 好双师服务器暂停提示接口
     * @return
     */
    @GET("mock_list")
    Call<List<String>> mockListLatex();
}

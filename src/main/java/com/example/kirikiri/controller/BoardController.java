package com.example.kirikiri.controller;

import com.example.kirikiri.domain.*;
import com.example.kirikiri.service.BoardService;
import com.example.kirikiri.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;


import java.util.List;
import java.io.File;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final UserService userService;

    @GetMapping("/all")
    public String getList(BoardDTO boardDTO, Integer page, Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        String userId = null;
        boolean userCheck;
        boolean profileCheck;
        UserVO userVO = null;

        model.addAttribute("popularPosts", boardService.getFivePopularPosts());
        model.addAttribute("popularWriters", boardService.getFivePopularWriters());

        if(session != null) {
            userId = (String) session.getAttribute("userId");
        }
        if(userId != null) {
            userCheck = true;
            userVO = userService.getUserVOById(userId);
            if(userVO.getUserProfile() != null) profileCheck = true;
            else profileCheck = false;
            model.addAttribute("userVO", userVO);
            model.addAttribute("profileCheck", profileCheck);
        }
        else userCheck = false;
        model.addAttribute("userCheck", userCheck);

        if(page == null) page = 1;
        Integer pageTotal = boardService.getCountAll();
        model.addAttribute("boards", boardService.getListAll(page));
        model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
        boardDTO.setCategoryName("All");
        return "/community";
    }
    @GetMapping("/board")
    public String moveCategory(BoardDTO boardDTO, Integer page, Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        String userId = null;
        boolean userCheck = false;
        boolean profileCheck = false;
        UserVO userVO = null;

        model.addAttribute("popularPosts", boardService.getFivePopularPosts());
        model.addAttribute("popularWriters", boardService.getFivePopularWriters());

        if(session != null) {
            userId = (String) session.getAttribute("userId");
        }
        if(userId != null) {
            userCheck = true;
            userVO = userService.getUserVOById(userId);
            if(userVO.getUserProfile() != null) profileCheck = true;
            else profileCheck = false;
            model.addAttribute("userVO", userVO);
            model.addAttribute("profileCheck", profileCheck);
        }
        else userCheck = false;
        model.addAttribute("userCheck", userCheck);

        BoardVO boardVO = boardDTO.changeBoardVO();
        Integer pageTotal;
        if(page == null) page = 1;
        boardDTO.setPage(page);
        if(boardDTO.getSortType() == 1) {
            if(boardDTO.getCategoryName().equals("All")){
                pageTotal = boardService.getCountAll();
                model.addAttribute("boards", boardService.getListAll(page));
                model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
            }
            else {
                if(boardDTO.getDetailCategoryName().isEmpty()) {
                    pageTotal = boardService.getCountCategory(boardVO);
                    model.addAttribute("boards", boardService.getListByCategory(boardDTO));
                    model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
                }
                else {
                    pageTotal = boardService.getCountDetailCategory(boardVO);
                    model.addAttribute("boards", boardService.getListByDetailCategory(boardDTO));
                    model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
                }
            }
        }
        if(boardDTO.getSortType() == 2) {
            if(boardDTO.getCategoryName().equals("All")){
                pageTotal = boardService.getCountAll();
                model.addAttribute("boards", boardService.getListAllOrderByLikes(page));
                model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
            } else{
                if(boardDTO.getDetailCategoryName().isEmpty()) {
                    pageTotal = boardService.getCountCategory(boardVO);
                    model.addAttribute("boards", boardService.getListByCategoryOrderByLikes(boardDTO));
                    model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
                }
                else {
                    pageTotal = boardService.getCountDetailCategory(boardVO);
                    model.addAttribute("boards", boardService.getListByDetailCategoryOrderByLikes(boardDTO));
                    model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
                }
            }
        }
        if(boardDTO.getSortType() == 3) {
            if(boardDTO.getCategoryName().equals("All")){
                pageTotal = boardService.getCountAll();
                model.addAttribute("boards", boardService.getListAllOrderByViews(page));
                model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
            } else {
                if(boardDTO.getDetailCategoryName().isEmpty()) {
                    pageTotal = boardService.getCountCategory(boardVO);
                    model.addAttribute("boards", boardService.getListByCategoryOrderByViews(boardDTO));
                    model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
                }
                else {
                    pageTotal = boardService.getCountDetailCategory(boardVO);
                    model.addAttribute("boards", boardService.getListByDetailCategoryOrderByViews(boardDTO));
                    model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
                }
            }
        }

        return "/community";
    }

    @GetMapping("/new")
    public String addPost(BoardVO boardVO, UserVO userVO, HttpServletRequest request){
        HttpSession session = request.getSession();
        String userId = "";
        boolean profileCheck;
        if(session != null) {
            userId = (String)session.getAttribute("userId");
        }
        else {
            return "/login";
        }
        if(userId != null) {
            UserVO user = userService.getUserVOById(userId);
            userVO.setUserNickname(user.getUserNickname());
            boardVO.setNationName(user.getUserNation());
            boardVO.setUserId(user.getUserId());
            return "/addPost";
        }
        else {
            return "/login";
        }
    }
    @PostMapping("/new")
    public RedirectView addPost(BoardVO boardVO){
        if(boardVO.getDetailCategoryName() == null) boardVO.setDetailCategoryName("Free");
        boardService.add(boardVO);
        return new RedirectView("/all");
    }




    @GetMapping("/post")
    public String post(Long boardId, Model model, HttpServletRequest request){
        HttpSession session = request.getSession();
        String userId = (String)session.getAttribute("userId");
        BoardVO boardVO = boardService.getBoard(boardId);
        boolean userCheck;
        boolean updateCheck;
        if(boardVO.getUserId().equals(userId)) userCheck = true;
        else userCheck = false;
        if(!boardVO.getBoardUpdateDate().equals(boardVO.getBoardRegisterDate())) updateCheck = true;
        else updateCheck = false;
        model.addAttribute("boardVO", boardVO);
        model.addAttribute("userCheck", userCheck);
        model.addAttribute("updateCheck", updateCheck);
        return "/post";
    }
    @GetMapping("/edit")
    public String editPost(Long boardId, Model model){
        BoardVO boardVO = boardService.getBoard(boardId);
        model.addAttribute("boardVO", boardVO);
        return "/editPost";
    }
    @PostMapping("/edit")
    public RedirectView editPost(BoardVO boardVO, RedirectAttributes redirectAttributes){
        boardService.edit(boardVO);
        redirectAttributes.addAttribute("boardId", boardVO.getBoardId());
        return new RedirectView("/post");
    }
    @GetMapping("/delete")
    public RedirectView deletePost(Long boardId) {
        boardService.delete(boardId);
        return new RedirectView("/all");
    }

    @GetMapping("/faq")
    public String FAQ(HttpServletRequest request, Model model){
        HttpSession session = request.getSession();
        String userId = null;
        boolean userCheck = false;
        boolean profileCheck = false;
        UserVO userVO = null;

        model.addAttribute("popularPosts", boardService.getFivePopularPosts());
        model.addAttribute("popularWriters", boardService.getFivePopularWriters());

        if(session != null) {
            userId = (String) session.getAttribute("userId");
        }
        if(userId != null) {
            userCheck = true;
            userVO = userService.getUserVOById(userId);
            if(userVO.getUserProfile() != null) profileCheck = true;
            else profileCheck = false;
            model.addAttribute("userVO", userVO);
            model.addAttribute("profileCheck", profileCheck);
        }
        else userCheck = false;
        model.addAttribute("userCheck", userCheck);
        return "/faq";
    }
    @GetMapping("/")
    public String main(HttpServletRequest request, Model model){
        HttpSession session = request.getSession();
        String userId = null;
        boolean userCheck = false;
        boolean profileCheck = false;
        UserVO userVO = null;

        model.addAttribute("popularPosts", boardService.getFivePopularPosts());
        model.addAttribute("popularWriters", boardService.getFivePopularWriters());

        if(session != null) {
            userId = (String) session.getAttribute("userId");
        }
        if(userId != null) {
            userCheck = true;
            userVO = userService.getUserVOById(userId);
            if(userVO.getUserProfile() != null) profileCheck = true;
            else profileCheck = false;
            model.addAttribute("userVO", userVO);
            model.addAttribute("profileCheck", profileCheck);
        }
        else userCheck = false;
        model.addAttribute("userCheck", userCheck);

        model.addAttribute("freeBoards", boardService.getFivePosts("Free"));
        model.addAttribute("counselingBoards", boardService.getFivePosts("Counseling"));
        model.addAttribute("playBoards", boardService.getFivePosts("Play"));
        model.addAttribute("jobBoards", boardService.getFivePosts("Job"));

        return "/mainPageHtml/index";
    }

    //    작성한 게시글 조회
    @GetMapping("/activity/writtenBoard")
    public String getWrittenBoard(String userId, Integer page, Model model) {
        if(page == null) page = 1;
        Integer pageTotal = boardService.getCountByUser(userId);
        PageDTO pbt = new PageDTO().createPageBoardDTO(page, pageTotal);
        model.addAttribute("pagination", pbt);
        model.addAttribute("boards", boardService.getWrittenBoard("aaa", pbt.getPage()));
        model.addAttribute("user", userService.getInfo("aaa"));
        return "/activity/writtenBoard";
    }

    @GetMapping("/activity/comment")
    public String getComment() {
        return "/activity/comment";
    }


    @GetMapping("/board/search")
    public String searchPosts(BoardDTO boardDTO, Integer page, Model model) {
        if(page == null) page = 1;
        Integer pageTotal = boardService.getCountSearchResult(boardDTO.getKeyword());
        boardDTO.setPage(page);
        model.addAttribute("boards", boardService.search(boardDTO));
        model.addAttribute("pagination", new PageDTO().createPageBoardDTO(page, pageTotal));
        return "/community";
    }
}

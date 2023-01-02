package com.example.kirikiri.controller;

import com.example.kirikiri.domain.BoardDTO;
import com.example.kirikiri.domain.BoardVO;
import com.example.kirikiri.domain.PageBoardDTO;
import com.example.kirikiri.domain.UserVO;
import com.example.kirikiri.service.BoardService;
import com.example.kirikiri.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;


import java.util.List;
import java.io.File;
import java.io.IOException;


@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;
    private final UserService userService;

    @GetMapping("/all")
    public String getList(BoardDTO boardDTO, Model model) {
        model.addAttribute("boards", boardService.getListAll());
        boardDTO.setCategoryName("All");
        return "/community";
    }

    @GetMapping("/board")
    public String moveCategory(BoardDTO boardDTO, Model model) {
        BoardVO boardVO = boardDTO.chageBoardVO();
        if (boardDTO.getSortType() == 1) {
            if (boardDTO.getCategoryName().equals("All")) {
                model.addAttribute("boards", boardService.getListAll());
            } else {
                if (boardDTO.getDetailCategoryName().isEmpty())
                    model.addAttribute("boards", boardService.getListByCategory(boardVO));
                else model.addAttribute("boards", boardService.getListByDetailCategory(boardVO));
            }
        }
        if (boardDTO.getSortType() == 2) {
            if (boardDTO.getCategoryName().equals("All")) {
                model.addAttribute("boards", boardService.getListAllOrderByLikes());
            } else {
                if (boardDTO.getDetailCategoryName().isEmpty())
                    model.addAttribute("boards", boardService.getListByCategoryOrderByLikes(boardVO));
                else model.addAttribute("boards", boardService.getListByDetailCategoryOrderByLikes(boardVO));
            }
        }
        if (boardDTO.getSortType() == 3) {
            if (boardDTO.getCategoryName().equals("All")) {
                model.addAttribute("boards", boardService.getListAllOrderByViews());
            } else {
                if (boardDTO.getDetailCategoryName().isEmpty())
                    model.addAttribute("boards", boardService.getListByCategoryOrderByViews(boardVO));
                else model.addAttribute("boards", boardService.getListByDetailCategoryOrderByViews(boardVO));
            }
        }

        return "/community";
    }

    @GetMapping("/board/new")
    public String addPost(BoardVO boardVO, UserVO userVO) {
        userVO.setUserId("aaa");
        userVO.setUserNickname("aaa");
        userVO.setUserNation("Japan");
        boardVO.setNationName(userVO.getUserNation());
        boardVO.setUserId(userVO.getUserId());
        return "/addPost";
    }

    @PostMapping("/board/new")
    public RedirectView addPost(BoardVO boardVO) {
        boardService.add(boardVO);
        return new RedirectView("/all");
    }

    @GetMapping("/post")
    public String post(Long boardId, Model model) {
        BoardVO boardVO = boardService.getBoard(boardId);
        boolean userCheck;
        boolean updateCheck;
        if (boardVO.getUserId().equals("aaa")) userCheck = true;
        else userCheck = false;
        if (!boardVO.getBoardUpdateDate().equals(boardVO.getBoardRegisterDate())) updateCheck = true;
        else updateCheck = false;
        model.addAttribute("boardVO", boardVO);
        model.addAttribute("userCheck", userCheck);
        model.addAttribute("updateCheck", updateCheck);
        return "/post";
    }

    @GetMapping("/edit")
    public String editPost(Long boardId, Model model) {
        BoardVO boardVO = boardService.getBoard(boardId);
        model.addAttribute("boardVO", boardVO);
        return "/editPost";
    }

    @PostMapping("/edit")
    public RedirectView editPost(BoardVO boardVO, RedirectAttributes redirectAttributes) {
        boardService.edit(boardVO);
        redirectAttributes.addAttribute("boardId", boardVO.getBoardId());
        return new RedirectView("/board/post");
    }

    @GetMapping("/delete")
    public RedirectView deletePost(Long boardId) {
        boardService.delete(boardId);
        return new RedirectView("/board/all");
    }

    //    작성한 게시글 조회
    @GetMapping("/activity/writtenBoard")
    public String getWrittenBoard(String userId, Integer page, Model model) {
        PageBoardDTO pbt = new PageBoardDTO().createPageBoardDTO(page, 255);
        model.addAttribute("pagination", pbt);
        model.addAttribute("boards", boardService.getWrittenBoard("kevs", pbt.getPage()));
        model.addAttribute("user", userService.getInfo("kevs"));
        return "/activity/writtenBoard";
    }

    @GetMapping("/activity/comment")
    public String getComment() {
        return "activity/comment";
    }


    @GetMapping("/board/search")
    public String searchPosts(BoardDTO boardDTO, Model model) {
        List<BoardVO> boards = boardService.search(boardDTO.getKeyword());
        model.addAttribute("boards", boards);
        return "/community";
    }
    @GetMapping("/display")
        public byte[] display (String path) throws IOException {
        return FileCopyUtils.copyToByteArray(new File("C:/upload", path));
        }

}

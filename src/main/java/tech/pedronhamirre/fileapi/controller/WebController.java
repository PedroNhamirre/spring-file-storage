package tech.pedronhamirre.fileapi.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import tech.pedronhamirre.fileapi.dto.FileUploadRequest;
import tech.pedronhamirre.fileapi.service.FileService;

@Controller
@RequestMapping("/")
public class WebController {

    private final FileService fileService;

    @Autowired
    public WebController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/")
    public String showUploadPage() {
        return "upload";
    }


    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            FileUploadRequest request = new FileUploadRequest(file);
            fileService.upload(request);
            redirectAttributes.addFlashAttribute("message", "Arquivo enviado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Erro: " + e.getMessage());
        }
        return "redirect:/";
    }

    @GetMapping("/list")
    public String listFiles(Model model) {
        model.addAttribute("files", fileService.getAllFiles());
        return "list";
    }
}
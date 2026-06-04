package cs.sbs.web.personalprojectweb2026.service;

import cs.sbs.web.personalprojectweb2026.model.entity.PromptTemplate;
import cs.sbs.web.personalprojectweb2026.repository.PromptTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptTemplateService {

    private final PromptTemplateRepository repository;

    public PromptTemplate getByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Prompt 模板不存在: " + name));
    }

    /**
     * Render a prompt template by replacing {variable} placeholders with values.
     */
    public RenderedPrompt render(String templateName, Map<String, String> variables) {
        PromptTemplate template = getByName(templateName);

        String systemPrompt = replaceVariables(template.getSystemPrompt(), variables);
        String userPrompt = replaceVariables(template.getUserPromptTemplate(), variables);

        return new RenderedPrompt(systemPrompt, userPrompt);
    }

    private String replaceVariables(String text, Map<String, String> variables) {
        String result = text;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }

    public record RenderedPrompt(String systemPrompt, String userPrompt) {}
}

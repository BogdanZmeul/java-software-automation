package compile;

import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import java.io.IOException;
import java.util.Set;

@SupportedAnnotationTypes("compile.GenerateValidation")
@SupportedSourceVersion(SourceVersion.RELEASE_24)
public class ValidationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element classElement : roundEnv.getElementsAnnotatedWith(GenerateValidation.class)) {

            GenerateValidation annotation = classElement.getAnnotation(GenerateValidation.class);
            String className = annotation.className();
            String packageName = processingEnv.getElementUtils().getPackageOf(classElement).toString();

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC)
                    .addJavadoc("Auto-generated DTO using JavaPoet framework.\n");

            MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC);

            for (Element enclosed : classElement.getEnclosedElements()) {
                if (enclosed.getKind() == ElementKind.FIELD) {

                    String fieldName = enclosed.getSimpleName().toString();
                    TypeName fieldType = TypeName.get(enclosed.asType());

                    FieldSpec.Builder fieldBuilder = FieldSpec.builder(fieldType, fieldName)
                            .addModifiers(Modifier.PRIVATE);

                    for (AnnotationMirror mirror : enclosed.getAnnotationMirrors()) {
                        fieldBuilder.addAnnotation(AnnotationSpec.get(mirror));
                    }
                    classBuilder.addField(fieldBuilder.build());

                    constructorBuilder.addParameter(fieldType, fieldName);
                    constructorBuilder.addStatement("this.$N = $N", fieldName, fieldName);

                    String capitalizedName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                    MethodSpec getter = MethodSpec.methodBuilder("get" + capitalizedName)
                            .addModifiers(Modifier.PUBLIC)
                            .returns(fieldType)
                            .addStatement("return this.$N", fieldName)
                            .build();

                    classBuilder.addMethod(getter);
                }
            }

            classBuilder.addMethod(constructorBuilder.build());

            JavaFile javaFile = JavaFile.builder(packageName, classBuilder.build())
                    .indent("    ")
                    .build();

            try {
                javaFile.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                System.err.println("Failed to generate class: " + className + " due to: " + e.getMessage());
            }
        }
        return true;
    }
}
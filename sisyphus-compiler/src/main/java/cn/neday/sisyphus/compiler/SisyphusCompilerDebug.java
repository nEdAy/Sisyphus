package cn.neday.sisyphus.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import cn.neday.sisyphus.Constants;
import cn.neday.sisyphus.annotation.Environment;
import cn.neday.sisyphus.annotation.Module;
import cn.neday.sisyphus.bean.EnvironmentBean;
import cn.neday.sisyphus.bean.ModuleBean;
import cn.neday.sisyphus.listener.OnEnvironmentChangeListener;

@AutoService(Processor.class)
public class SisyphusCompilerDebug extends AbstractProcessor {

    public static final String ENVIRONMENT = "Environment";
    public static final String METHOD_NAME_GET_XX_ENVIRONMENT = "get%sEnvironment";
    public static final String METHOD_NAME_GET_XX_ENVIRONMENT_BEAN = "get%sEnvironmentBean";
    public static final String METHOD_NAME_SET_XX_ENVIRONMENT = "set%sEnvironment";
    public static final String METHOD_NAME_ADD_ON_ENVIRONMENT_CHANGE_LISTENER = "addOnEnvironmentChangeListener";
    public static final String METHOD_NAME_REMOVE_ON_ENVIRONMENT_CHANGE_LISTENER = "removeOnEnvironmentChangeListener";
    public static final String METHOD_NAME_REMOVE_ALL_ON_ENVIRONMENT_CHANGE_LISTENER = "removeAllOnEnvironmentChangeListener";
    public static final String METHOD_NAME_ON_ENVIRONMENT_CHANGE = "onEnvironmentChange";
    public static final String MODE_PRIVATE = "android.content.Context.MODE_PRIVATE";
    public static final String VAR_CONTEXT = "context";
    public static final String VAR_ENVIRONMENT_VALUE_SUFFIX = "Value";
    public static final String VAR_ENVIRONMENT_NAME_SUFFIX = "Name";
    public static final String VAR_ENVIRONMENT_ALIAS_SUFFIX = "Alias";
    public static final String VAR_MODULE_PREFIX = "MODULE_";
    public static final String VAR_DEFAULT_ENVIRONMENT_PREFIX = "DEFAULT_";
    public static final String VAR_DEFAULT_ENVIRONMENT_SUFFIX = "_ENVIRONMENT";
    public static final String VAR_CURRENT_XX_ENVIRONMENT = "sCurrent%sEnvironment";
    public static final String VAR_MODULE_LIST = "MODULE_LIST";
    public static final String VAR_ON_ENVIRONMENT_CHANGE_LISTENERS = "ON_ENVIRONMENT_CHANGE_LISTENERS";
    public static final String VAR_PARAMETER_ON_ENVIRONMENT_CHANGE_LISTENER = "onEnvironmentChangeListener";
    public static final String VAR_PARAMETER_MODULE = "module";
    public static final String VAR_PARAMETER_OLD_ENVIRONMENT = "oldEnvironment";
    public static final String VAR_PARAMETER_NEW_ENVIRONMENT = "newEnvironment";
    public static final TypeName CONTEXT_TYPE_NAME = ClassName.get("android.content", "Context");

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        TypeSpec.Builder sisyphusClassBuilder = TypeSpec
                .classBuilder(Constants.SISYPHUS_FILE_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        FieldSpec onEnvironmentChangeListenersFiled = FieldSpec
                .builder(ArrayList.class, VAR_ON_ENVIRONMENT_CHANGE_LISTENERS)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(String.format("new %s<%s>()", ArrayList.class.getSimpleName(), OnEnvironmentChangeListener.class.getSimpleName()))
                .build();
        sisyphusClassBuilder.addField(onEnvironmentChangeListenersFiled);

        MethodSpec addOnEnvironmentChangeListenerMethod = MethodSpec
                .methodBuilder(METHOD_NAME_ADD_ON_ENVIRONMENT_CHANGE_LISTENER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(OnEnvironmentChangeListener.class, VAR_PARAMETER_ON_ENVIRONMENT_CHANGE_LISTENER)
                .addStatement(String.format("%s.add(%s)", VAR_ON_ENVIRONMENT_CHANGE_LISTENERS, VAR_PARAMETER_ON_ENVIRONMENT_CHANGE_LISTENER))
                .build();
        sisyphusClassBuilder.addMethod(addOnEnvironmentChangeListenerMethod);

        MethodSpec removeOnEnvironmentChangeListenerMethod = MethodSpec
                .methodBuilder(METHOD_NAME_REMOVE_ON_ENVIRONMENT_CHANGE_LISTENER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(OnEnvironmentChangeListener.class, VAR_PARAMETER_ON_ENVIRONMENT_CHANGE_LISTENER)
                .addStatement(String.format("%s.remove(%s)", VAR_ON_ENVIRONMENT_CHANGE_LISTENERS, VAR_PARAMETER_ON_ENVIRONMENT_CHANGE_LISTENER))
                .build();
        sisyphusClassBuilder.addMethod(removeOnEnvironmentChangeListenerMethod);

        MethodSpec removeAllOnEnvironmentChangeListenerMethod = MethodSpec
                .methodBuilder(METHOD_NAME_REMOVE_ALL_ON_ENVIRONMENT_CHANGE_LISTENER)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addStatement(String.format("%s.clear()", VAR_ON_ENVIRONMENT_CHANGE_LISTENERS))
                .build();
        sisyphusClassBuilder.addMethod(removeAllOnEnvironmentChangeListenerMethod);

        MethodSpec onEnvironmentChangeMethod = MethodSpec
                .methodBuilder(METHOD_NAME_ON_ENVIRONMENT_CHANGE)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(void.class)
                .addParameter(ModuleBean.class, VAR_PARAMETER_MODULE)
                .addParameter(EnvironmentBean.class, VAR_PARAMETER_OLD_ENVIRONMENT)
                .addParameter(EnvironmentBean.class, VAR_PARAMETER_NEW_ENVIRONMENT)
                .addCode(String.format(
                        "for (Object onEnvironmentChangeListener : %s) {\n" +
                                "   if (onEnvironmentChangeListener instanceof %s) {\n" +
                                "       ((%s) onEnvironmentChangeListener).onEnvironmentChanged(%s, %s, %s);\n" +
                                "   }\n" +
                                "}\n", VAR_ON_ENVIRONMENT_CHANGE_LISTENERS,
                        OnEnvironmentChangeListener.class.getSimpleName(),
                        OnEnvironmentChangeListener.class.getSimpleName(), VAR_PARAMETER_MODULE, VAR_PARAMETER_OLD_ENVIRONMENT, VAR_PARAMETER_NEW_ENVIRONMENT))
                .build();
        sisyphusClassBuilder.addMethod(onEnvironmentChangeMethod);

        FieldSpec moduleListField = FieldSpec
                .builder(ArrayList.class, VAR_MODULE_LIST, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(String.format("new %s<%s>()", ArrayList.class.getSimpleName(), ModuleBean.class.getSimpleName()))
                .build();
        sisyphusClassBuilder.addField(moduleListField);

        MethodSpec.Builder getModuleListMethodBuilder = MethodSpec
                .methodBuilder(Constants.METHOD_NAME_GET_MODULE_LIST)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ArrayList.class);

        CodeBlock.Builder staticCodeBlockBuilder = CodeBlock.builder();

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Module.class);
        for (Element element : elements) {
            Module moduleAnnotation = element.getAnnotation(Module.class);
            if (moduleAnnotation == null) {
                continue;
            }
            String moduleName = element.getSimpleName().toString();
            String moduleUpperCaseName = moduleName.toUpperCase();
            String moduleLowerCaseName = moduleName.toLowerCase();
            String moduleAliasName = moduleAnnotation.alias();

            FieldSpec moduleXXField = FieldSpec
                    .builder(ModuleBean.class, String.format("%s%s", VAR_MODULE_PREFIX, moduleUpperCaseName))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer(String.format("new %s(\"%s\", \"%s\")", ModuleBean.class.getSimpleName(), moduleName, moduleAliasName))
                    .build();
            sisyphusClassBuilder.addField(moduleXXField);

            staticCodeBlockBuilder
                    .add("\n")
                    .addStatement(String.format("%s.add(%s%s)", VAR_MODULE_LIST, VAR_MODULE_PREFIX, moduleUpperCaseName));

            FieldSpec xxModuleCurrentEnvironmentField = FieldSpec
                    .builder(EnvironmentBean.class, String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName))
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .build();
            sisyphusClassBuilder.addField(xxModuleCurrentEnvironmentField);

            MethodSpec getXXEnvironmentMethod = MethodSpec
                    .methodBuilder(String.format(METHOD_NAME_GET_XX_ENVIRONMENT, moduleName))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .returns(String.class)
                    .addParameter(CONTEXT_TYPE_NAME, VAR_CONTEXT)
                    .addStatement(String.format("return get%sEnvironmentBean(%s).getValue()", moduleName, VAR_CONTEXT))
                    .build();
            sisyphusClassBuilder.addMethod(getXXEnvironmentMethod);

            MethodSpec getXXEnvironmentBeanMethod = MethodSpec
                    .methodBuilder(String.format(METHOD_NAME_GET_XX_ENVIRONMENT_BEAN, moduleName))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .returns(EnvironmentBean.class)
                    .addParameter(CONTEXT_TYPE_NAME, VAR_CONTEXT)
                    .addCode(String.format(
                            "if (%s == null) {\n" +
                                    "    android.content.SharedPreferences sharedPreferences = null;\n" +
                                    "    try {\n" +
                                    "       androidx.security.crypto.MasterKey mainKey = new androidx.security.crypto.MasterKey.Builder(%s)\n" +
                                    "           .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)\n" +
                                    "           .build();\n" +
                                    "       sharedPreferences = androidx.security.crypto.EncryptedSharedPreferences.create(\n" +
                                    "           %s,\n" +
                                    "           %s.getPackageName() + \".%s\",\n" +
                                    "           mainKey,\n" +
                                    "           androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,\n" +
                                    "           androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM\n" +
                                    "       );\n" +
                                    "    } catch (java.security.GeneralSecurityException | java.io.IOException e) {\n" +
                                    "       sharedPreferences = %s.getSharedPreferences(%s.getPackageName() + \".%s\", %s);\n" +
                                    "    }\n" +
                                    "    String value = sharedPreferences.getString(\"%s%s%s\", %s%s%s.getValue());\n" +
                                    "    String environmentName = sharedPreferences.getString(\"%s%s%s\", %s%s%s.getName());\n" +
                                    "    String alias = sharedPreferences.getString(\"%s%s%s\", %s%s%s.getAlias());\n" +
                                    "    for (EnvironmentBean environmentBean : MODULE_%s.getEnvironments()) {\n" +
                                    "        if (android.text.TextUtils.equals(environmentBean.getValue(), value)\n" +
                                    "                && android.text.TextUtils.equals(environmentBean.getName(), environmentName)\n" +
                                    "                && android.text.TextUtils.equals(environmentBean.getAlias(), alias)) {\n" +
                                    "            %s = environmentBean;\n" +
                                    "            break;\n" +
                                    "        }\n" +
                                    "    }\n" +
                                    "    if (%s == null) {\n" +
                                    "        %s = %s%s%s;\n" +
                                    "    }\n" +
                                    "}\n",
                            String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName),
                            VAR_CONTEXT, VAR_CONTEXT, VAR_CONTEXT, Constants.SISYPHUS_FILE_NAME.toLowerCase(),
                            VAR_CONTEXT, VAR_CONTEXT, Constants.SISYPHUS_FILE_NAME.toLowerCase(), MODE_PRIVATE,
                            moduleLowerCaseName, ENVIRONMENT, VAR_ENVIRONMENT_VALUE_SUFFIX, VAR_DEFAULT_ENVIRONMENT_PREFIX, moduleUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX,
                            moduleLowerCaseName, ENVIRONMENT, VAR_ENVIRONMENT_NAME_SUFFIX, VAR_DEFAULT_ENVIRONMENT_PREFIX, moduleUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX,
                            moduleLowerCaseName, ENVIRONMENT, VAR_ENVIRONMENT_ALIAS_SUFFIX, VAR_DEFAULT_ENVIRONMENT_PREFIX, moduleUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX,
                            moduleUpperCaseName,
                            String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName),
                            String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName),
                            String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName),
                            VAR_DEFAULT_ENVIRONMENT_PREFIX, moduleUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX))
                    .addStatement(String.format("return " + VAR_CURRENT_XX_ENVIRONMENT, moduleName))
                    .build();
            sisyphusClassBuilder.addMethod(getXXEnvironmentBeanMethod);

            MethodSpec setXXEnvironmentMethod = MethodSpec.methodBuilder(String.format(METHOD_NAME_SET_XX_ENVIRONMENT, moduleName))
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .returns(void.class)
                    .addParameter(CONTEXT_TYPE_NAME, VAR_CONTEXT)
                    .addParameter(EnvironmentBean.class, VAR_PARAMETER_NEW_ENVIRONMENT)
                    .addStatement(String.format(
                            "android.content.SharedPreferences sharedPreferences = null;\n" +
                                    "try {\n" +
                                    "   androidx.security.crypto.MasterKey mainKey = new androidx.security.crypto.MasterKey.Builder(%s)\n" +
                                    "       .setKeyScheme(androidx.security.crypto.MasterKey.KeyScheme.AES256_GCM)\n" +
                                    "       .build();\n" +
                                    "       sharedPreferences = androidx.security.crypto.EncryptedSharedPreferences.create(\n" +
                                    "       %s,\n" +
                                    "       %s.getPackageName() + \".%s\",\n" +
                                    "       mainKey,\n" +
                                    "       androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,\n" +
                                    "       androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM\n" +
                                    "   );\n" +
                                    "} catch (java.security.GeneralSecurityException | java.io.IOException e) {\n" +
                                    "   sharedPreferences = %s.getSharedPreferences(%s.getPackageName() + \".%s\", %s);\n" +
                                    "}\n" +
                                    "sharedPreferences.edit()\n" +
                                    ".putString(\"%s%s%s\", %s.getValue())\n" +
                                    ".putString(\"%s%s%s\", %s.getName())\n" +
                                    ".putString(\"%s%s%s\", %s.getAlias())\n" +
                                    ".commit()",
                            VAR_CONTEXT, VAR_CONTEXT, VAR_CONTEXT, Constants.SISYPHUS_FILE_NAME.toLowerCase(),
                            VAR_CONTEXT, VAR_CONTEXT, Constants.SISYPHUS_FILE_NAME.toLowerCase(), MODE_PRIVATE,
                            moduleLowerCaseName, ENVIRONMENT, VAR_ENVIRONMENT_VALUE_SUFFIX, VAR_PARAMETER_NEW_ENVIRONMENT,
                            moduleLowerCaseName, ENVIRONMENT, VAR_ENVIRONMENT_NAME_SUFFIX, VAR_PARAMETER_NEW_ENVIRONMENT,
                            moduleLowerCaseName, ENVIRONMENT, VAR_ENVIRONMENT_ALIAS_SUFFIX, VAR_PARAMETER_NEW_ENVIRONMENT
                    ))
                    .addCode(String.format(
                            "if (!%s.equals(%s)) {\n" +
                                    "    EnvironmentBean oldEnvironment = %s;\n" +
                                    "    %s = %s;\n" +
                                    "    onEnvironmentChange(%s%s, oldEnvironment, %s);\n" +
                                    "}\n", VAR_PARAMETER_NEW_ENVIRONMENT, String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName),
                            String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName),
                            String.format(VAR_CURRENT_XX_ENVIRONMENT, moduleName), VAR_PARAMETER_NEW_ENVIRONMENT,
                            VAR_MODULE_PREFIX, moduleUpperCaseName, VAR_PARAMETER_NEW_ENVIRONMENT))
                    .build();
            sisyphusClassBuilder.addMethod(setXXEnvironmentMethod);

            FieldSpec.Builder defaultXXEnvironmentFiledBuilder = FieldSpec
                    .builder(EnvironmentBean.class, String.format("%s%s%s", VAR_DEFAULT_ENVIRONMENT_PREFIX, moduleUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX),
                            Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            List<? extends Element> allMembers = processingEnv.getElementUtils().getAllMembers((TypeElement) element);

            for (Element member : allMembers) {
                Environment environmentAnnotation = member.getAnnotation(Environment.class);
                if (environmentAnnotation == null) {
                    continue;
                }

                String environmentName = member.getSimpleName().toString();
                String environmentUpperCaseName = environmentName.toUpperCase();
                String value = environmentAnnotation.value();
                String alias = environmentAnnotation.alias();

                FieldSpec environmentField = generateEnvironmentField(environmentAnnotation, defaultXXEnvironmentFiledBuilder,
                        moduleUpperCaseName, environmentName, environmentUpperCaseName, value, alias);

                sisyphusClassBuilder.addField(environmentField);

                staticCodeBlockBuilder
                        .addStatement(String.format("%s%s.getEnvironments().add(%s)", VAR_MODULE_PREFIX, moduleUpperCaseName, String.format("%s_%s%s", moduleUpperCaseName, environmentUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX)));
            }

            sisyphusClassBuilder.addField(defaultXXEnvironmentFiledBuilder.build()).build();
        }

        getModuleListMethodBuilder.addStatement(String.format("return %s", VAR_MODULE_LIST));

        sisyphusClassBuilder.addMethod(getModuleListMethodBuilder.build());

        sisyphusClassBuilder.addStaticBlock(staticCodeBlockBuilder.build());

        JavaFile sisyphusJavaFile = JavaFile.builder(Constants.PACKAGE_NAME, sisyphusClassBuilder.build()).build();

        try {
            sisyphusJavaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    protected FieldSpec generateEnvironmentField(Environment environmentAnnotation,
                                                 FieldSpec.Builder defaultXXEnvironmentFiledBuilder,
                                                 String moduleUpperCaseName,
                                                 String environmentName,
                                                 String environmentUpperCaseName,
                                                 String value,
                                                 String alias) {
        if (environmentAnnotation.isDefault()) {
            defaultXXEnvironmentFiledBuilder.initializer(String.format("%s_%s%s", moduleUpperCaseName, environmentUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX));
        }
        return FieldSpec
                .builder(EnvironmentBean.class,
                        String.format("%s_%s%s", moduleUpperCaseName, environmentUpperCaseName, VAR_DEFAULT_ENVIRONMENT_SUFFIX),
                        Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(String.format("new %s(\"%s\", \"%s\", \"%s\", %s%s)",
                        EnvironmentBean.class.getSimpleName(), environmentName, value, alias, VAR_MODULE_PREFIX, moduleUpperCaseName))
                .build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(Module.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
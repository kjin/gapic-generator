/* Copyright 2016 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.api.codegen.gapic;

import com.google.api.codegen.InterfaceView;
import com.google.api.codegen.SnippetSetRunner;
import com.google.api.codegen.clientconfig.ClientConfigGapicContext;
import com.google.api.codegen.clientconfig.ClientConfigSnippetSetRunner;
import com.google.api.codegen.clientconfig.php.PhpClientConfigGapicContext;
import com.google.api.codegen.config.GapicProductConfig;
import com.google.api.codegen.config.PackageMetadataConfig;
import com.google.api.codegen.nodejs.NodeJSCodePathMapper;
import com.google.api.codegen.php.PhpGapicCodePathMapper;
import com.google.api.codegen.rendering.CommonSnippetSetRunner;
import com.google.api.codegen.transformer.csharp.CSharpGapicClientTransformer;
import com.google.api.codegen.transformer.csharp.CSharpGapicSmokeTestTransformer;
import com.google.api.codegen.transformer.csharp.CSharpGapicSnippetsTransformer;
import com.google.api.codegen.transformer.csharp.CSharpGapicUnitTestTransformer;
import com.google.api.codegen.transformer.go.GoGapicSurfaceTestTransformer;
import com.google.api.codegen.transformer.go.GoGapicSurfaceTransformer;
import com.google.api.codegen.transformer.java.JavaGapicMetadataTransformer;
import com.google.api.codegen.transformer.java.JavaGapicSamplesTransformer;
import com.google.api.codegen.transformer.java.JavaGapicSurfaceTransformer;
import com.google.api.codegen.transformer.java.JavaSurfaceTestTransformer;
import com.google.api.codegen.transformer.nodejs.NodeJSGapicSurfaceDocTransformer;
import com.google.api.codegen.transformer.nodejs.NodeJSGapicSurfaceTestTransformer;
import com.google.api.codegen.transformer.nodejs.NodeJSGapicSurfaceTransformer;
import com.google.api.codegen.transformer.nodejs.NodeJSPackageMetadataTransformer;
import com.google.api.codegen.transformer.php.PhpGapicSurfaceTestTransformer;
import com.google.api.codegen.transformer.php.PhpGapicSurfaceTransformer;
import com.google.api.codegen.transformer.php.PhpPackageMetadataTransformer;
import com.google.api.codegen.transformer.py.PythonGapicSamplesTransformer;
import com.google.api.codegen.transformer.py.PythonGapicSurfaceTestTransformer;
import com.google.api.codegen.transformer.py.PythonGapicSurfaceTransformer;
import com.google.api.codegen.transformer.py.PythonPackageMetadataTransformer;
import com.google.api.codegen.transformer.ruby.RubyGapicSurfaceDocTransformer;
import com.google.api.codegen.transformer.ruby.RubyGapicSurfaceTestTransformer;
import com.google.api.codegen.transformer.ruby.RubyGapicSurfaceTransformer;
import com.google.api.codegen.transformer.ruby.RubyPackageMetadataTransformer;
import com.google.api.codegen.util.CommonRenderingUtil;
import com.google.api.codegen.util.csharp.CSharpNameFormatter;
import com.google.api.codegen.util.csharp.CSharpRenderingUtil;
import com.google.api.codegen.util.java.JavaRenderingUtil;
import com.google.api.codegen.util.py.PythonRenderingUtil;
import com.google.api.codegen.util.ruby.RubyNameFormatter;
import com.google.api.tools.framework.model.Interface;
import com.google.api.tools.framework.model.Model;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.NotImplementedException;

/** MainGapicProviderFactory creates GapicProvider instances based on an id. */
public class MainGapicProviderFactory implements GapicProviderFactory {

  public static final String CLIENT_CONFIG = "client_config";
  public static final String CSHARP = "csharp";
  public static final String GO = "go";
  public static final String JAVA = "java";
  public static final String NODEJS = "nodejs";
  public static final String NODEJS_DOC = "nodejs_doc";
  public static final String PHP = "php";
  public static final String PYTHON = "py";
  public static final String RUBY = "ruby";
  public static final String RUBY_DOC = "ruby_doc";

  /** Create the GapicProviders based on the given id */
  public static List<GapicProvider<?>> defaultCreate(
      Model model,
      GapicProductConfig productConfig,
      GapicGeneratorConfig generatorConfig,
      PackageMetadataConfig packageConfig) {

    ArrayList<GapicProvider<?>> providers = new ArrayList<>();
    String id = generatorConfig.id();
    // Please keep the following IDs in alphabetical order
    if (id.equals(CLIENT_CONFIG)) {
      GapicProvider provider =
          CommonGapicProvider.<Interface>newBuilder()
              .setModel(model)
              .setView(new InterfaceView())
              .setContext(new ClientConfigGapicContext(model, productConfig))
              .setSnippetSetRunner(
                  new ClientConfigSnippetSetRunner<>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
              .setSnippetFileNames(Arrays.asList("clientconfig/json.snip"))
              .setCodePathMapper(CommonGapicCodePathMapper.defaultInstance())
              .build();
      providers.add(provider);
    } else if (id.equals(CSHARP)) {
      String packageName = productConfig.getPackageName();
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicCodePathMapper pathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix(packageName + File.separator + packageName)
                .setPackageFilePathNameFormatter(new CSharpNameFormatter())
                .build();
        GapicProvider mainProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CSharpRenderingUtil()))
                .setModelToViewTransformer(
                    new CSharpGapicClientTransformer(pathMapper, packageConfig))
                .build();
        providers.add(mainProvider);
        GapicCodePathMapper snippetPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix(packageName + File.separator + packageName + ".Snippets")
                .setPackageFilePathNameFormatter(new CSharpNameFormatter())
                .build();
        GapicProvider snippetProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CSharpRenderingUtil()))
                .setModelToViewTransformer(new CSharpGapicSnippetsTransformer(snippetPathMapper))
                .build();
        providers.add(snippetProvider);
      }
      if (generatorConfig.enableTestGenerator()) {
        GapicCodePathMapper smokeTestPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix(packageName + File.separator + packageName + ".SmokeTests")
                .setPackageFilePathNameFormatter(new CSharpNameFormatter())
                .build();
        GapicProvider smokeTestProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CSharpRenderingUtil()))
                .setModelToViewTransformer(new CSharpGapicSmokeTestTransformer(smokeTestPathMapper))
                .build();
        providers.add(smokeTestProvider);
        GapicCodePathMapper unitTestPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix(packageName + File.separator + packageName + ".Tests")
                .setPackageFilePathNameFormatter(new CSharpNameFormatter())
                .build();
        GapicProvider unitTestProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CSharpRenderingUtil()))
                .setModelToViewTransformer(new CSharpGapicUnitTestTransformer(unitTestPathMapper))
                .build();
        providers.add(unitTestProvider);
      }

    } else if (id.equals(GO)) {
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicProvider provider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new GoGapicSurfaceTransformer(new PackageNameCodePathMapper()))
                .build();
        providers.add(provider);
      }
      if (generatorConfig.enableTestGenerator()) {
        GapicProvider testProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(new GoGapicSurfaceTestTransformer())
                .build();
        providers.add(testProvider);
      }

    } else if (id.equals(JAVA)) {
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicCodePathMapper javaPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix("src/main/java")
                .setShouldAppendPackage(true)
                .build();
        GapicProvider mainProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new JavaRenderingUtil()))
                .setModelToViewTransformer(
                    new JavaGapicSurfaceTransformer(javaPathMapper, packageConfig))
                .build();

        providers.add(mainProvider);

        GapicProvider metadataProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new JavaRenderingUtil()))
                .setModelToViewTransformer(new JavaGapicMetadataTransformer(packageConfig))
                .build();
        providers.add(metadataProvider);

        GapicProvider staticResourcesProvider =
            new StaticResourcesProvider(
                ImmutableMap.<String, String>builder()
                    .put("java/static/build.gradle", "../build.gradle")
                    .put("java/static/settings.gradle", "../settings.gradle")
                    .put("java/static/gradlew", "../gradlew")
                    .put("java/static/gradlew.bat", "../gradlew.bat")
                    .put(
                        "java/static/gradle/wrapper/gradle-wrapper.jar",
                        "../gradle/wrapper/gradle-wrapper.jar")
                    .put(
                        "java/static/gradle/wrapper/gradle-wrapper.properties",
                        "../gradle/wrapper/gradle-wrapper.properties")
                    .build(),
                ImmutableSet.of("../gradlew"));
        providers.add(staticResourcesProvider);

        GapicProvider sampleProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new JavaRenderingUtil()))
                .setModelToViewTransformer(new JavaGapicSamplesTransformer(javaPathMapper))
                .build();
        providers.add(sampleProvider);
      }
      if (generatorConfig.enableTestGenerator()) {
        GapicCodePathMapper javaTestPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix("src/test/java")
                .setShouldAppendPackage(true)
                .build();
        GapicProvider testProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new JavaSurfaceTestTransformer(
                        javaTestPathMapper,
                        new JavaGapicSurfaceTransformer(javaTestPathMapper, packageConfig),
                        "java/grpc_test.snip"))
                .build();
        providers.add(testProvider);
      }
      return providers;

    } else if (id.equals(NODEJS) || id.equals(NODEJS_DOC)) {
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicCodePathMapper nodeJSPathMapper = new NodeJSCodePathMapper();
        GapicProvider mainProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new NodeJSGapicSurfaceTransformer(nodeJSPathMapper, packageConfig))
                .build();
        GapicProvider metadataProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(new NodeJSPackageMetadataTransformer(packageConfig))
                .build();
        GapicProvider clientConfigProvider =
            CommonGapicProvider.<Interface>newBuilder()
                .setModel(model)
                .setView(new InterfaceView())
                .setContext(new ClientConfigGapicContext(model, productConfig))
                .setSnippetSetRunner(
                    new ClientConfigSnippetSetRunner<>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
                .setSnippetFileNames(Arrays.asList("clientconfig/json.snip"))
                .setCodePathMapper(nodeJSPathMapper)
                .build();

        providers.add(mainProvider);
        providers.add(metadataProvider);
        providers.add(clientConfigProvider);

        if (id.equals(NODEJS_DOC)) {
          GapicProvider messageProvider =
              ViewModelGapicProvider.newBuilder()
                  .setModel(model)
                  .setProductConfig(productConfig)
                  .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                  .setModelToViewTransformer(new NodeJSGapicSurfaceDocTransformer())
                  .build();
          providers.add(messageProvider);
        }
      }
      if (generatorConfig.enableTestGenerator()) {
        GapicProvider testProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(new NodeJSGapicSurfaceTestTransformer())
                .build();
        providers.add(testProvider);
      }

    } else if (id.equals(PHP)) {
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicCodePathMapper phpPathMapper =
            PhpGapicCodePathMapper.newBuilder().setPrefix("src").build();
        GapicProvider provider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new PhpGapicSurfaceTransformer(productConfig, phpPathMapper, model))
                .build();

        GapicCodePathMapper phpClientConfigPathMapper =
            PhpGapicCodePathMapper.newBuilder().setPrefix("src").setSuffix("resources").build();
        GapicProvider clientConfigProvider =
            CommonGapicProvider.<Interface>newBuilder()
                .setModel(model)
                .setView(new InterfaceView())
                .setContext(new PhpClientConfigGapicContext(model, productConfig))
                .setSnippetSetRunner(
                    new ClientConfigSnippetSetRunner<>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
                .setSnippetFileNames(Arrays.asList("clientconfig/json.snip"))
                .setCodePathMapper(phpClientConfigPathMapper)
                .build();

        GapicProvider metadataProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(new PhpPackageMetadataTransformer(packageConfig))
                .build();

        providers.add(provider);
        providers.add(clientConfigProvider);
        providers.add(metadataProvider);
      }
      if (generatorConfig.enableTestGenerator()) {
        GapicProvider testProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(new PhpGapicSurfaceTestTransformer(packageConfig))
                .build();
        providers.add(testProvider);
      }

    } else if (id.equals(PYTHON)) {
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicCodePathMapper pythonPathMapper =
            CommonGapicCodePathMapper.newBuilder().setShouldAppendPackage(true).build();
        GapicProvider mainProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new PythonRenderingUtil()))
                .setModelToViewTransformer(
                    new PythonGapicSurfaceTransformer(pythonPathMapper, packageConfig))
                .build();
        GapicProvider sampleProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new PythonRenderingUtil()))
                .setModelToViewTransformer(new PythonGapicSamplesTransformer(pythonPathMapper))
                .build();
        GapicProvider clientConfigProvider =
            CommonGapicProvider.<Interface>newBuilder()
                .setModel(model)
                .setView(new InterfaceView())
                .setContext(new ClientConfigGapicContext(model, productConfig))
                .setSnippetSetRunner(
                    new ClientConfigSnippetSetRunner<>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
                .setSnippetFileNames(Arrays.asList("clientconfig/python_clientconfig.snip"))
                .setCodePathMapper(pythonPathMapper)
                .build();
        providers.add(mainProvider);
        providers.add(sampleProvider);
        providers.add(clientConfigProvider);

        GapicProvider metadataProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new PythonRenderingUtil()))
                .setModelToViewTransformer(new PythonPackageMetadataTransformer(packageConfig))
                .build();
        providers.add(metadataProvider);
      }
      if (generatorConfig.enableTestGenerator()) {
        GapicCodePathMapper pythonTestPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix("test")
                .setShouldAppendPackage(true)
                .build();
        GapicProvider testProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new PythonGapicSurfaceTestTransformer(pythonTestPathMapper, packageConfig))
                .build();
        providers.add(testProvider);
      }

    } else if (id.equals(RUBY) || id.equals(RUBY_DOC)) {
      if (generatorConfig.enableSurfaceGenerator()) {
        GapicCodePathMapper rubyPathMapper =
            CommonGapicCodePathMapper.newBuilder()
                .setPrefix("lib")
                .setShouldAppendPackage(true)
                .setPackageFilePathNameFormatter(new RubyNameFormatter())
                .build();
        GapicProvider mainProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new RubyGapicSurfaceTransformer(rubyPathMapper, packageConfig))
                .build();
        GapicProvider clientConfigProvider =
            CommonGapicProvider.<Interface>newBuilder()
                .setModel(model)
                .setView(new InterfaceView())
                .setContext(new ClientConfigGapicContext(model, productConfig))
                .setSnippetSetRunner(
                    new ClientConfigSnippetSetRunner<>(SnippetSetRunner.SNIPPET_RESOURCE_ROOT))
                .setSnippetFileNames(Arrays.asList("clientconfig/json.snip"))
                .setCodePathMapper(rubyPathMapper)
                .build();
        GapicProvider metadataProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(new RubyPackageMetadataTransformer(packageConfig))
                .build();

        providers.add(mainProvider);
        providers.add(clientConfigProvider);
        providers.add(metadataProvider);

        if (id.equals(RUBY_DOC)) {
          GapicProvider messageProvider =
              ViewModelGapicProvider.newBuilder()
                  .setModel(model)
                  .setProductConfig(productConfig)
                  .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                  .setModelToViewTransformer(
                      new RubyGapicSurfaceDocTransformer(rubyPathMapper, packageConfig))
                  .build();
          providers.add(messageProvider);
        }
      }
      if (generatorConfig.enableTestGenerator()) {
        CommonGapicCodePathMapper.Builder rubyTestPathMapperBuilder =
            CommonGapicCodePathMapper.newBuilder()
                .setShouldAppendPackage(true)
                .setPackageFilePathNameFormatter(new RubyNameFormatter());
        GapicProvider testProvider =
            ViewModelGapicProvider.newBuilder()
                .setModel(model)
                .setProductConfig(productConfig)
                .setSnippetSetRunner(new CommonSnippetSetRunner(new CommonRenderingUtil()))
                .setModelToViewTransformer(
                    new RubyGapicSurfaceTestTransformer(
                        rubyTestPathMapperBuilder.setPrefix("test").build(),
                        rubyTestPathMapperBuilder.setPrefix("acceptance").build(),
                        packageConfig))
                .build();
        providers.add(testProvider);
      }
    } else {
      throw new NotImplementedException("GapicProviderFactory: invalid id \"" + id + "\"");
    }

    if (providers.isEmpty()) {
      throw new IllegalArgumentException("No artifacts are enabled.");
    }
    return providers;
  }

  /** Create the GapicProviders based on the given id */
  @Override
  public List<GapicProvider<?>> create(
      Model model,
      GapicProductConfig productConfig,
      GapicGeneratorConfig generatorConfig,
      PackageMetadataConfig packageConfig) {
    return defaultCreate(model, productConfig, generatorConfig, packageConfig);
  }
}

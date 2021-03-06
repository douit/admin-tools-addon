/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.addon.admintools.web.screens.console

import com.haulmont.cuba.gui.WindowParam
import com.haulmont.cuba.gui.components.FileUploadField
import com.haulmont.cuba.gui.components.HBoxLayout
import com.haulmont.cuba.gui.components.SourceCodeEditor
import com.haulmont.cuba.gui.components.VBoxLayout
import com.haulmont.cuba.gui.xml.layout.ComponentsFactory
import de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType
import de.diedavids.cuba.runtimediagnose.web.screens.console.ConsoleWindow
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream

import javax.inject.Inject

import static com.haulmont.cuba.gui.components.Frame.NotificationType.ERROR
import static de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType.JPQL
import static de.diedavids.cuba.runtimediagnose.diagnose.DiagnoseType.SQL
import static java.nio.charset.StandardCharsets.UTF_8
import static org.apache.commons.io.FilenameUtils.getExtension
import static org.apache.commons.io.IOUtils.closeQuietly
import static org.apache.commons.io.IOUtils.toString
import static org.apache.commons.lang3.StringUtils.isNotBlank

class ConsoleFrameExtended extends ConsoleWindow {
    @Inject
    protected SourceCodeEditor console
    @Inject
    protected VBoxLayout consoleVBox
    @Inject
    protected ComponentsFactory componentsFactory;

    @WindowParam(name = 'diagnoseType')
    protected DiagnoseType diagnoseType

    protected FileUploadField uploadField

    @Override
    void init(Map<String, Object> params) {
        super.init(params)

        uploadField = componentsFactory.createComponent(FileUploadField.class)
        uploadField.mode = FileUploadField.FileStoragePutMode.MANUAL
        uploadField.setUploadButtonIcon('icons/upload.png')
        uploadField.setUploadButtonCaption(getMessage('uploadDiagnoseRequestFile'))
        uploadField.addFileUploadSucceedListener({ e -> uploadFile() })

        HBoxLayout hbox1 = consoleVBox.getComponentNN(0)
        HBoxLayout hbox2 = hbox1.getComponentNN(0)
        hbox2.setSpacing(true)
        hbox2.add(uploadField)

        console.setValue(params.getOrDefault('script', ''))
        this.setHeightFull()
        this.setWidthFull()
    }

    protected void uploadFile() {
        def extension = uploadField.fileDescriptor.extension

        if ("zip" != extension) {
            showNotification(getMessage("extensionError"))
            return
        }

        InputStream fileContent = uploadField.getFileContent()

        try {
            String script = getScriptFromZip(fileContent)
            if (isNotBlank(script)) {
                console.setValue(script)
            }
        } catch (Exception e) {
            showNotification(e.getLocalizedMessage(), ERROR)
        } finally {
            closeQuietly(fileContent)
        }
    }

    protected String getScriptFromZip(InputStream inputStream) {
        ZipArchiveInputStream archiveReader = new ZipArchiveInputStream(inputStream)

        try {
            ZipArchiveEntry entry
            while ((entry = archiveReader.getNextZipEntry()) != null) {
                String extension = getExtension(entry.getName())

                if ('jpql' == extension && diagnoseType == JPQL ||
                        'sql' == extension && diagnoseType == SQL) {

                    return toString(archiveReader, UTF_8)
                }
            }

            return ''
        }
        finally {
            closeQuietly(archiveReader)
        }
    }

}

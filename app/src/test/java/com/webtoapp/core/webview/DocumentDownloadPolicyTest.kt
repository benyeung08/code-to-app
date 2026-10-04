package com.webtoapp.core.webview

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DocumentDownloadPolicyTest {

    @Test
    fun `document urls are flagged as unrenderable`() {
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("http://127.0.0.1:8080/report.pdf")).isTrue()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/files/合同.DOCX")).isTrue()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/app.apk?token=1")).isTrue()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/a/b/archive.zip#frag")).isTrue()
    }

    @Test
    fun `renderable urls are not flagged`() {
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("http://127.0.0.1:8080/")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/index.html")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/api/data.json")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/docs/page.php?id=report.pdf")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://example.com/img.photo.jpg")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("https://cdn.example.com/v1.2/app")).isFalse()
    }

    @Test
    fun `non http schemes are never flagged`() {
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("file:///docs/report.pdf")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("content://media/file.pdf")).isFalse()
        assertThat(DocumentDownloadPolicy.isUnrenderableDocumentUrl("blob:https://example.com/abc")).isFalse()
    }

    @Test
    fun `mime types map to the detected extension`() {
        assertThat(DocumentDownloadPolicy.mimeTypeFor("https://example.com/a.pdf"))
            .isEqualTo("application/pdf")
        assertThat(DocumentDownloadPolicy.mimeTypeFor("https://example.com/a.PDF"))
            .isEqualTo("application/pdf")
        assertThat(DocumentDownloadPolicy.mimeTypeFor("https://example.com/a.apk"))
            .isEqualTo("application/vnd.android.package-archive")
        assertThat(DocumentDownloadPolicy.mimeTypeFor("https://example.com/a.weird"))
            .isEqualTo("application/octet-stream")
    }

    @Test
    fun `loopback hosts are detected`() {
        assertThat(DocumentDownloadPolicy.isLoopbackUrl("http://127.0.0.1:8080/file.pdf")).isTrue()
        assertThat(DocumentDownloadPolicy.isLoopbackUrl("http://localhost:3000/")).isTrue()
        assertThat(DocumentDownloadPolicy.isLoopbackUrl("http://[::1]:9000/x")).isTrue()
        assertThat(DocumentDownloadPolicy.isLoopbackUrl("https://example.com/file")).isFalse()
        assertThat(DocumentDownloadPolicy.isLoopbackUrl("http://127.0.0.1.example.com/")).isFalse()
        assertThat(DocumentDownloadPolicy.isLoopbackUrl("file:///localhost/x")).isFalse()
    }
}

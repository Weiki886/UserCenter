# PowerShell脚本：批量修复BaseResponse导入问题

# 定义项目路径
$PROJECT_PATH = "D:\Projects\PersonalProjects\UserCenter"

# 查找使用旧版BaseResponse的Java文件
function Find-Files {
    Get-ChildItem -Path $PROJECT_PATH -Filter "*.java" -Recurse | 
    Select-String -Pattern "import com.weiki.usercenterbackend.common.BaseResponse;" -List |
    Select-Object -ExpandProperty Path
}

# 替换导入语句
function Replace-Imports {
    Write-Host "替换以下文件中的BaseResponse导入:" -ForegroundColor Yellow
    
    $files = Find-Files
    foreach ($file in $files) {
        Write-Host "- $file"
        
        # 读取文件内容
        $content = Get-Content -Path $file -Raw
        
        # 替换导入语句
        $newContent = $content -replace "import com.weiki.usercenterbackend.common.BaseResponse;", "import com.weiki.usercenterbackend.model.response.BaseResponse;"
        
        # 保存修改后的内容
        Set-Content -Path $file -Value $newContent
        
        # 验证替换是否成功
        if (Select-String -Path $file -Pattern "import com.weiki.usercenterbackend.model.response.BaseResponse;" -Quiet) {
            Write-Host "  √ 替换成功" -ForegroundColor Green
        } else {
            Write-Host "  × 替换失败" -ForegroundColor Red
        }
    }
}

Write-Host "开始处理BaseResponse导入问题..." -ForegroundColor Cyan
Replace-Imports
Write-Host "处理完成!" -ForegroundColor Cyan

# 列出可能仍然存在类型问题的文件
Write-Host "`n检查是否还有其他BaseResponse类型问题..." -ForegroundColor Cyan
$problemFiles = Get-ChildItem -Path $PROJECT_PATH -Filter "*.java" -Recurse | 
                Select-String -Pattern "com.weiki.usercenterbackend.common.BaseResponse" -List |
                Select-Object -ExpandProperty Path

if ($problemFiles) {
    Write-Host "以下文件可能仍然存在BaseResponse类型问题:" -ForegroundColor Yellow
    foreach ($file in $problemFiles) {
        Write-Host "- $file"
    }
} else {
    Write-Host "未发现其他BaseResponse类型问题!" -ForegroundColor Green
} 
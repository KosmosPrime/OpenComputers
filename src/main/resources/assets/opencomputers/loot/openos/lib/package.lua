local package = {}

package.config = "/\n;\n?\n!\n-\n"

package.path = "/lib/?.lua;/usr/lib/?.lua;/home/lib/?.lua;./?.lua;/lib/?/init.lua;/usr/lib/?/init.lua;/home/lib/?/init.lua;./?/init.lua"

local loading = {}
local preload = {}
local searchers = {}

local loaded = {
  ["_G"] = _G,
  ["bit32"] = bit32,
  ["coroutine"] = coroutine,
  ["math"] = math,
  ["os"] = os,
  ["package"] = package,
  ["string"] = string,
  ["table"] = table
}
package.loaded = loaded
package.preload = preload
package.searchers = searchers

function package.searchpath(name, path, sep, rep)
  checkArg(1, name, "string")
  checkArg(2, path, "string")
  sep = sep or '.'
  rep = rep or '/'
  sep, rep = '%' .. sep, rep
  name = string.gsub(name, sep, rep)
  local fs = require("filesystem")
  local errorFiles = {}
  for subPath in string.gmatch(path, "([^;]+)") do
    subPath = string.gsub(subPath, "?", name)
    if subPath:sub(1, 1) ~= "/" and os.getenv then
      subPath = fs.concat(os.getenv("PWD") or "/", subPath)
    end
    if fs.exists(subPath) then
      local file = fs.open(subPath, "r")
      if file then
        file:close()
        return subPath
      end
    end
    table.insert(errorFiles, "no file '" .. subPath .. "'")
  end
  return nil, table.concat(errorFiles, "\n\t")
end

table.insert(searchers, function(module)
  if package.preload[module] then
    return package.preload[module]
  end

  return "no field package.preload['" .. module .. "']"
end)
table.insert(searchers, function(module)
  local library, path, status

  path, status = package.searchpath(module, package.path)
  if not path then
    return status
  end

  library, status = loadfile(path)
  if not library then
    error(string.format("error loading module '%s' from file '%s':\n\t%s", module, path, status))
  end

  return library, module
end)

function require(module)
  checkArg(1, module, "string")
  if loaded[module] ~= nil then
    return loaded[module]
  elseif loading[module] then
    error("already loading: " .. module .. "\n" .. debug.traceback(), 2)
  else
    local library, status, arg
    local errors = ""

    if type(searchers) ~= "table" then error("'package.searchers' must be a table") end
    for _, searcher in pairs(searchers) do
      library, arg = searcher(module)
      if type(library) == "function" then break end
      if type(library) ~= nil then
        errors = errors .. "\n\t" .. tostring(library)
        library = nil
      end
    end
    if not library then error(string.format("module '%s' not found:%s", module, errors)) end

    loading[module] = true
    library, status = pcall(library, arg or module)
    loading[module] = false
    assert(library, string.format("module '%s' load failed:\n%s", module, status))
    loaded[module] = status
    return status
  end
end

function package.delay(lib, file)
  local mt = {}
  function mt.__index(tbl, key)
    mt.__index = nil
    dofile(file)
    return tbl[key]
  end
  if lib.internal then
    setmetatable(lib.internal, mt)
  end
  setmetatable(lib, mt)
end

-------------------------------------------------------------------------------

return package
